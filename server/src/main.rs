#![feature(collections_drain)]
extern crate libc;
extern crate rustc_serialize;
extern crate time;
extern crate websocket;
use ode_bindgen::dReal;
use rustc_serialize::json;
use std::collections::HashMap;
use std::collections::hash_map::Entry::{Vacant, Occupied};
use std::error::Error;
use std::io;
use std::io::{BufRead, BufReader, Write};
use std::net::{TcpListener, TcpStream};
use std::ops::Add;
use std::sync::mpsc::{channel, Sender};
use std::thread::spawn;
use time::{Duration, get_time};
use websocket::result::WebSocketError;
use websocket::ws::message::Message as WSMessage;
use websocket::ws::receiver::Receiver as WSReceiver;

static TAU: dReal = (2.0 * std::f64::consts::PI) as dReal;

// hack around cargo not respecting "proper" link attributes
#[link(name = "ode")] extern {}
mod ode_bindgen;

#[derive(RustcEncodable, RustcDecodable, Clone, Debug, Copy)]
//pub struct Position { x: dReal, y: dReal, z: dReal }
pub struct Position (dReal, dReal, dReal);

impl Add for Position {
    type Output = Position;
    fn add(self, other: Position) -> Position {
        let Position(x1, y1, z1) = self;
        let Position(x2, y2, z2) = other;
        Position(x1 + x2, y1 + y2, z1 + z2)
    }
}
#[derive(RustcEncodable, RustcDecodable, Clone, Debug, Copy)]
//pub struct Orientation { theta: dReal, phi: dReal }
pub struct Orientation (dReal, dReal);

impl Add for Orientation {
    type Output = Orientation;
    fn add(self, other: Orientation) -> Orientation {
        let Orientation(t1, p1) = self;
        let Orientation(t2, p2) = other;
        Orientation(t1+t2, p1+p2)
    }
}

#[derive(RustcEncodable, RustcDecodable, Clone, Copy)]
pub enum PlayerCommand {
    RequestModel(ObjectType), // TODO: some kind of DoS mitigation?
    MoveForward(dReal),
    MoveSideways(dReal),
    MoveUp(dReal), //possibly replace with "Jump" when transitioning to non-free-movement?
    RotateCamera(Orientation),
    Shoot,
}

#[derive(RustcEncodable, RustcDecodable, Clone, Copy, Debug)]
pub enum ObjectType {
    Floor,
    Sphere, Cylinder, Triprism, // arbitrary geometric obstacles
    Player,
    Bullet,
}

#[derive(RustcEncodable, RustcDecodable, Clone, Debug)]
pub enum ServerCommand {
    ProvideModel(ObjectType, Vec<dReal>),
    SetPosition(i64, Position),
    SetOrientation(i64, Orientation),
    AddObject(i64, GameObject),
    RemoveObject(i64),
    SetPlayerNumber(i64),
    InitializeWorld(HashMap<i64, GameObject>),
}

pub enum ServerControlMsg {
    StartConnection(Box<GameClientReader+Send>, Box<GameClientWriter+Send>),
    IndividualCommand(i64, ServerCommand),
    BroadcastCommand(ServerCommand),
    ProcessPlayerAction(i64, PlayerCommand),
    DisconnectPlayer(i64),
    Tick(Duration),
}

#[derive(RustcEncodable, RustcDecodable, Clone)]
pub struct IncomingMessage {
    command: PlayerCommand,
    timestamp: i64,
}

#[derive(RustcEncodable, RustcDecodable, Clone)]
pub struct OutgoingMessage {
    command: ServerCommand,
    timestamp: i64,
}

#[derive(RustcEncodable, RustcDecodable, Clone, Debug)]
pub struct GameObject {
    pos: Position,
    ori: Orientation,
    obj_type: ObjectType,
}

fn example_playercommands() -> Vec<PlayerCommand> { vec!(
    PlayerCommand::MoveForward(0.5),
    PlayerCommand::MoveSideways(0.6),
    PlayerCommand::RotateCamera(Orientation(-1.5, 0.2)),
    PlayerCommand::Shoot,
)}

fn example_servercommands() -> Vec<ServerCommand> { vec!(
    ServerCommand::SetPosition(42, Position(50.0, 10.0, 25.0)),
    ServerCommand::SetOrientation(42, Orientation(0.0, 0.0)),
    ServerCommand::AddObject(42, GameObject {
        pos: Position(0.0, 0.0, 0.0),
        ori: Orientation(0.0, 0.0),
        obj_type: ObjectType::Player
    }),
    ServerCommand::RemoveObject(42),
)}

fn show_examples(mut stream: TcpStream, playernum: i64) {
    let seconds = get_time().sec;
    println!("Received a connection from {:?} at time {:?} (player {:?}).", stream.peer_addr(), seconds, playernum);
    //writeln!(stream, "{}", &json::encode(&IncomingMessage{command: PlayerCommand::MoveForward(0.5), timestamp: 0}).unwrap()).unwrap();
    for cmd in example_servercommands().iter() {
        writeln!(stream, "{}", &json::encode(&OutgoingMessage{command: cmd.clone(), timestamp: seconds}).unwrap()).unwrap();
    }
    for &cmd in example_playercommands().iter() {
        writeln!(stream, "{}", &json::encode(&IncomingMessage{command: cmd, timestamp: seconds}).unwrap()).unwrap();
    }
}

pub trait GameClientReader {
    fn receive_message(&mut self) -> Option<Result<PlayerCommand, Box<Error>>>;
}

pub trait GameClientWriter {
    fn send_message(&mut self, &ServerCommand) -> Result<(), Box<Error>>;
}

impl GameClientReader for Box<GameClientReader+Send> {
    fn receive_message(&mut self) -> Option<Result<PlayerCommand, Box<Error>>> {
        (**self).receive_message()
    }
}
impl GameClientWriter for Box<GameClientWriter+Send> {
    fn send_message(&mut self, command: &ServerCommand) -> Result<(), Box<Error>> {
        (**self).send_message(command)
    }
}

impl GameClientReader for io::Lines<BufReader<TcpStream>> {
    fn receive_message(&mut self) -> Option<Result<PlayerCommand, Box<Error>>> {
        /*self.by_ref().next().map(|res| res.map(|s| json::decode(&s).unwrap_or_else(|e| {
            println!("Ignoring bad input ({:?}): \"{}\"", e, s);
            // this returns from the innermost scope, is there a way to return from outermost scope?
            return None;
        })))*/
        match self.by_ref().next() {
            None => None,
            Some(res) => Some(match res {
                Err(e) => Err(Box::new(e)),
                Ok(s) => match json::decode(&s) {
                    Err(e) => {
                        println!("Ignoring bad input ({:?}): \"{}\"", e, s);
                        return self.receive_message();
                    }
                    Ok(cmd) => Ok(cmd)
                }
            })
        }
    }
}

impl GameClientWriter for TcpStream {
    fn send_message(&mut self, message: &ServerCommand) -> Result<(), Box<Error>> {
        writeln!(self, "{}", &json::encode(message).unwrap())
            .map_err(|e| Box::new(e) as Box<Error>)
            .map(|_| ())
    }
}

impl GameClientReader for websocket::server::receiver::Receiver<websocket::stream::WebSocketStream> {
    fn receive_message(&mut self) -> Option<Result<PlayerCommand, Box<Error>>> {
        fn try_decode(rec: &mut websocket::server::receiver::Receiver<websocket::stream::WebSocketStream>,
                        s: String) -> Option<Result<PlayerCommand, Box<Error>>> {
            match json::decode(&s) {
                Err(e) => {
                    println!("Ignoring bad input ({:?}): \"{}\"", e, s);
                    rec.receive_message()
                },
                Ok(cmd) => Some(Ok(cmd))
            }
        };
        match self.recv_message_dataframes() {
            Err(WebSocketError::NoDataAvailable) => None,
            Err(e) => Some(Err(Box::new(e))),
            Ok(frames) => match websocket::message::Message::from_dataframes(frames) {
                Err(e) => Some(Err(Box::new(e))),
                Ok(websocket::message::Message::Text(s)) => try_decode(self, s),
                Ok(websocket::message::Message::Binary(bs)) => match String::from_utf8(bs) {
                    Ok(s) => try_decode(self, s),
                    Err(e) => Some(Err(Box::new(e)))
                },
                Ok(_) => None
            }
        }
    }
}
impl GameClientWriter for websocket::server::sender::Sender<websocket::stream::WebSocketStream> {
    fn send_message(&mut self, message: &ServerCommand) -> Result<(), Box<Error>> {
        websocket::ws::sender::Sender::send_message(self,
            websocket::Message::Text(json::encode(message).unwrap())
        ).map_err(|e| Box::new(e) as Box<Error>)
    }
}

fn process_input_from_client<C: GameClientReader>(client: &mut C,
                            playernum: i64,
                            transmit_servctl: Sender<ServerControlMsg>) {
    while let Some(res) = client.receive_message() {
        match res {
            Ok(command) => {
                transmit_servctl.send(ServerControlMsg::ProcessPlayerAction(playernum, command)).unwrap();
            }
            Err(e) => { println!("Some error occurred reading a line: {:?}", e); }
        }
    }
    transmit_servctl.send(ServerControlMsg::DisconnectPlayer(playernum)).unwrap();
}

fn send_initialization_to_client<C: GameClientWriter>(client: &mut C,
                                 playernum: i64,
                                 world: &HashMap<i64, GameObject>) -> Result<(), Box<Error>> {
    try!(client.send_message(&ServerCommand::SetPlayerNumber(playernum)));
    try!(client.send_message(&ServerCommand::InitializeWorld(world.clone())));
    Ok(())
}

fn send_action_to_client<C: GameClientWriter>(client: &mut C, playernum: i64, action: &ServerCommand) -> Result<(), Box<Error>> {
    println!("{}", format!("Sending {:?} to client {}", action, playernum));
    try!(client.send_message(action));
    Ok(())
}

// TODO: consider using lazy_static! instead
fn get_mesh(ty: ObjectType) -> Vec<dReal> {
    match ty {
        ObjectType::Floor => json::decode(include_str!("../../modelmaker/floor_model.json")).unwrap(),
        ObjectType::Sphere => json::decode(include_str!("../../modelmaker/unit_sphere.json")).unwrap(),
        ObjectType::Cylinder => json::decode(include_str!("../../modelmaker/unit_cylinder.json")).unwrap(),
        ObjectType::Triprism => json::decode(include_str!("../../modelmaker/unit_triprism.json")).unwrap(),
        ObjectType::Player => json::decode(include_str!("../../modelmaker/player_model.json")).unwrap(),
        ObjectType::Bullet => unimplemented!(),
    }
}

fn get_player(world: &mut HashMap<i64, GameObject>,
                playerid: i64,
                broadcast: Sender<ServerControlMsg>) -> &mut GameObject {
    let player = match world.entry(playerid) {
        Vacant(entry) => {
            let newplayer = GameObject {
                pos: Position(0.0, 0.0, 0.0),
                ori: Orientation(0.0, 0.0),
                obj_type: ObjectType::Player,
            };
            broadcast.send(ServerControlMsg::BroadcastCommand(
                ServerCommand::AddObject(playerid, newplayer.clone())
            )).unwrap();
            entry.insert(newplayer)
        }
        Occupied(entry) => { entry.into_mut() }
    };
    player
}

fn apply_polar_movement(pos: Position, magnitude: dReal, theta: dReal) -> Position {
    let Position(x, y, z) = pos;
    Position(x + magnitude*theta.cos(), y, z + magnitude*theta.sin())
}

fn get_cost_of_action(action: PlayerCommand) -> dReal {
    match action {
        PlayerCommand::RequestModel(_) => 0.0,
        PlayerCommand::MoveForward(x) => x.abs(),
        PlayerCommand::MoveSideways(x) => x.abs(),
        PlayerCommand::MoveUp(x) => x.abs(),
        PlayerCommand::RotateCamera(_) => 0.0,
        PlayerCommand::Shoot => 0.0,
    }
}

fn process_player_action(world: &mut HashMap<i64, GameObject>,
                         sender: Sender<ServerControlMsg>,
                         playerid: i64,
                         action: PlayerCommand) {
    let broadcast_location = |obj: &GameObject, i: i64| {
        sender.send(ServerControlMsg::BroadcastCommand(ServerCommand::SetPosition(i, obj.pos))).unwrap();
    };
    match action {
        PlayerCommand::MoveForward(delta) => {
            println!("Player #{:?} moves {:?} units forward", playerid, delta);
            let player = get_player(world, playerid, sender.clone());
            let Orientation(theta, _) = player.ori;
            player.pos = apply_polar_movement(player.pos, delta, -theta + TAU/4.0);
            println!("P#{:?} pos: {:?}", playerid, player.pos);
            broadcast_location(player, playerid);
        }
        PlayerCommand::MoveSideways(delta) => {
            println!("Player #{:?} moves {:?} units to their right", playerid, delta);
            let player = get_player(world, playerid, sender.clone());
            let Orientation(theta, _) = player.ori;
            player.pos = apply_polar_movement(player.pos, delta, -theta);
            println!("P#{:?} pos: {:?}", playerid, player.pos);
            broadcast_location(player, playerid);
        }
        PlayerCommand::MoveUp(delta) => {
            println!("Player #{:?} moves {:?} units up", playerid, delta);
            let player = get_player(world, playerid, sender.clone());
            player.pos = player.pos + Position(0.0, delta, 0.0);
            println!("P#{:?} pos: {:?}", playerid, player.pos);
            broadcast_location(player, playerid);
        }
        PlayerCommand::RotateCamera(Orientation(theta, phi)) => {
            println!("Player #{:?} rotates by ({:?}, {:?})", playerid, theta, phi);
            let player = &mut get_player(world, playerid, sender.clone());
            player.ori = player.ori + Orientation(theta, phi);
            println!("P#{:?} ori: {:?}", playerid, player.ori);
            sender.send(ServerControlMsg::BroadcastCommand(
                ServerCommand::SetOrientation(playerid, player.ori)
            )).unwrap();
        }
        PlayerCommand::Shoot => { println!("Player #{:?} shoots", playerid); },
        PlayerCommand::RequestModel(ty) => sender.send(ServerControlMsg::IndividualCommand(playerid,
            ServerCommand::ProvideModel(ty, get_mesh(ty)))).unwrap(),
    }
}

fn ode_main_test() {
    // transcribed from ODE's "demo_buggy" example
    unsafe {
        ode_bindgen::dInitODE();
        let world = ode_bindgen::dWorldCreate();
        let space = ode_bindgen::dHashSpaceCreate(0 as *mut ode_bindgen::Struct_dxSpace);
        let ground = ode_bindgen::dCreatePlane(space, 0.0, 1.0, 0.0, 0.0);
        ode_bindgen::dCloseODE();
    }
}

fn listener_loop_tcp(sender: Sender<ServerControlMsg>) {
    let listener = TcpListener::bind(("0.0.0.0", 51701)).unwrap(); //large number for port chosen pseudorandomly
    for stream in listener.incoming() {
        match stream {
            Err(e) => { println!("Error accepting incoming TCP connection: {:?}", e); },
            Ok(stream) => {
                println!("Accepted a TCP connection from {:?}.", stream.peer_addr());
                let reader = BufReader::new(stream.try_clone().unwrap()).lines();
                sender.send(ServerControlMsg::StartConnection(Box::new(reader), Box::new(stream))).unwrap();
            },
        }
    }
}

fn listener_loop_ws(sender: Sender<ServerControlMsg>) {
    let listener = websocket::Server::bind(("0.0.0.0", 51702)).unwrap();
    for connection in listener {
        match connection.and_then(|c| c.read_request()) {
            Err(e) => println!("Error reading incoming WS request: {:?}", e),
            Ok(request) => match request.accept().send().map(|client| client.split()) {
                Err(e) => println!("Error accepting incoming WS connection: {:?}", e),
                Ok((writer, mut reader)) => {
                    println!("Accepted a WS connection from {:?}.", reader.get_mut().peer_addr());
                    sender.send(ServerControlMsg::StartConnection(Box::new(reader), Box::new(writer))).unwrap();
                }
            }
        }
    }
}

fn timer_loop(sender: Sender<ServerControlMsg>) {
    let mut last_time = get_time();
    loop {
        let cur_time = get_time();
        let elapsed = cur_time - last_time;
        if elapsed >= Duration::milliseconds(10) {
            last_time = cur_time;
            sender.send(ServerControlMsg::Tick(elapsed)).unwrap();
        }
    }
}

fn place_initial_obstacles(world: &mut HashMap<i64, GameObject>) {
    world.insert(-1, GameObject {
        pos: Position(0.0, -1.0, 0.0),
        ori: Orientation(0.0, 0.0),
        obj_type: ObjectType::Floor,
    });
    world.insert(-2, GameObject {
        pos: Position(-5.0, 10.0, 0.0),
        ori: Orientation(0.0, 0.0),
        obj_type: ObjectType::Sphere,
    });
    world.insert(-3, GameObject {
        pos: Position(0.0, 10.0, 5.0),
        ori: Orientation(0.0, 0.0),
        obj_type: ObjectType::Cylinder,
    });
    world.insert(-4, GameObject {
        pos: Position(5.0, 10.0, 0.0),
        ori: Orientation(0.0, 0.0),
        obj_type: ObjectType::Triprism,
    });
}

// contains some code adapted from example at http://doc.rust-lang.org/std/net/struct.TcpListener.html
fn main() {
    unsafe { ode_bindgen::dInitODE(); }
    println!("current time: {:?}", get_time());
    println!("address of dWorldCreate: {:p}", &ode_bindgen::dWorldCreate);
    let mut world = HashMap::<i64, GameObject>::new();
    let mut playernum: i64 = 0;

    println!("{:?}", std::str::from_utf8(unsafe {
        std::ffi::CStr::from_ptr(ode_bindgen::dGetConfiguration()).to_bytes()
    }));

    println!("{:?}", get_mesh(ObjectType::Floor));

    place_initial_obstacles(&mut world);

    let (transmit_servctl, receive_servctl) = channel();
    { let tx = transmit_servctl.clone(); spawn(move || { listener_loop_tcp(tx); }); }
    { let tx = transmit_servctl.clone(); spawn(move || { listener_loop_ws(tx); }); }
    { let tx = transmit_servctl.clone(); spawn(move || { timer_loop(tx); }); }

    let mut connections = HashMap::<i64, Box<GameClientWriter+Send>>::new();

    let mut action_buffer = Vec::new();

    let mut minimum_tick_time = Duration::days(1);
    let mut maximum_tick_time = Duration::nanoseconds(0);
    let mut total_elapsed_time = Duration::nanoseconds(0);
    let mut total_elapsed_ticks = 0;

    for servctl in receive_servctl.iter() {
        match servctl {
            ServerControlMsg::StartConnection(mut reader, mut writer) => {
                playernum += 1;
                send_initialization_to_client(&mut writer, playernum, &world).unwrap();
                connections.insert(playernum, writer);
                // create & broadcast creation of player object
                drop(get_player(&mut world, playernum, transmit_servctl.clone()));
                {
                    let tx = transmit_servctl.clone();
                    spawn(move || { process_input_from_client(&mut reader, playernum, tx); });
                }
            },
            ServerControlMsg::IndividualCommand(playernum, action) => {
                if let Some(Err(e)) = connections.get_mut(&playernum)
                    .map(|w| send_action_to_client(w, playernum, &action)) {
                    println!("Sending {:?} to client {} failed ({:?}).", action, playernum, e);
                }
            },
            ServerControlMsg::BroadcastCommand(action) => {
                for (&playernum, stream) in connections.iter_mut() {
                    if let Err(e) = send_action_to_client(stream, playernum, &action) {
                        println!("Sending {:?} to client {} failed ({:?}).", action, playernum, e);
                    }
                }
            },
            ServerControlMsg::ProcessPlayerAction(pid, action) => {
                action_buffer.push((pid, action));
            }
            ServerControlMsg::DisconnectPlayer(pid) => {
                world.remove(&pid);
                connections.remove(&pid);
                transmit_servctl.send(ServerControlMsg::BroadcastCommand(
                    ServerCommand::RemoveObject(pid)
                )).unwrap();
            }
            ServerControlMsg::Tick(elapsed) => {
                total_elapsed_time = total_elapsed_time + elapsed;
                total_elapsed_ticks += 1;
                if elapsed < minimum_tick_time {
                    minimum_tick_time = elapsed;
                }
                if elapsed > maximum_tick_time {
                    maximum_tick_time = elapsed;
                }
                if total_elapsed_ticks % 100 == 1 {
                    if let Some(ns) = total_elapsed_time.num_nanoseconds() {
                        println!("The current nanosecond/tick average is {}ns.", ns/total_elapsed_ticks);
                    }
                    if let Some(ns) = minimum_tick_time.num_nanoseconds() {
                        println!("The shortest tick so far was {}ns.", ns);
                    }
                    if let Some(ns) = maximum_tick_time.num_nanoseconds() {
                        println!("The longest tick so far was {}ns.", ns);
                    }
                }
                let movement_budget: dReal = 1.0;
                let mut budgets_spent = HashMap::<i64, dReal>::new();
                for (pid, action) in action_buffer.drain(..) {
                    let cur_cost = get_cost_of_action(action);
                    match budgets_spent.entry(pid) {
                        Occupied(mut spent) => {
                            if spent.get() + cur_cost <= movement_budget {
                                *spent.get_mut() += cur_cost;
                                process_player_action(&mut world, transmit_servctl.clone(), pid, action);
                            }
                        }
                        Vacant(spent) => {
                            if cur_cost < movement_budget {
                                spent.insert(cur_cost);
                                process_player_action(&mut world, transmit_servctl.clone(), pid, action);
                            }
                        }
                    }
                }
            }
        }
    }
    unsafe { ode_bindgen::dCloseODE(); }
}
