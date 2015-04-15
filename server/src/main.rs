extern crate libc;
extern crate rustc_serialize;
extern crate time;
use rustc_serialize::json;
use std::collections::HashMap;
use std::collections::hash_map::Entry::{Vacant, Occupied};
use std::io;
use std::io::{BufRead, BufStream, Write};
use std::net::{TcpListener, TcpStream};
use std::ops::Add;
use std::sync::mpsc::{channel, Sender};
use std::thread::spawn;

static PI: f64 = std::f64::consts::PI;
static TAU: f64 = 2f64 * std::f64::consts::PI;

// hack around cargo not respecting "proper" link attributes
#[link(name = "ode")] extern {}
mod ode_bindgen;

#[derive(RustcEncodable, RustcDecodable, Clone, Debug, Copy)]
//pub struct Position { x: f64, y: f64, z: f64 }
pub struct Position (f64, f64, f64);

impl Add for Position {
    type Output = Position;
    fn add(self, other: Position) -> Position {
        let Position(x1, y1, z1) = self;
        let Position(x2, y2, z2) = other;
        Position(x1 + x2, y1 + y2, z1 + z2)
    }
}
#[derive(RustcEncodable, RustcDecodable, Clone, Debug, Copy)]
//pub struct Orientation { theta: f64, phi: f64 }
pub struct Orientation (f64, f64);

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
    MoveForward(f64),
    MoveSideways(f64),
    MoveUp(f64), //possibly replace with "Jump" when transitioning to non-free-movement?
    RotateCamera(Orientation),
    Shoot,
}

#[derive(RustcEncodable, RustcDecodable, Clone, Copy, Debug)]
pub enum ObjectType {
    Floor,
    Obstacle(i64),
    Player,
    Bullet,
}

#[derive(RustcEncodable, RustcDecodable, Clone, Debug)]
pub enum ServerCommand {
    SetPosition(i64, Position),
    SetOrientation(i64, Orientation),
    AddObject(i64, Position, Orientation, ObjectType),
    RemoveObject(i64),
    SetPlayerNumber(i64),
    InitializeWorld(HashMap<i64, GameObject>),
}

pub enum ServerControlMsg {
    StartConnection(TcpStream),
    BroadcastCommand(ServerCommand),
    ProcessPlayerAction(i64, PlayerCommand),
    DisconnectPlayer(i64),
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

/*fn createPlayer(&mut world: &mut HashMap<i64, GameObject>) {
}*/

fn example_playercommands() -> Vec<PlayerCommand> { vec!(
    PlayerCommand::MoveForward(0.5),
    PlayerCommand::MoveSideways(0.6),
    PlayerCommand::RotateCamera(Orientation(-1.5, 0.2)),
    PlayerCommand::Shoot,
)}

fn example_servercommands() -> Vec<ServerCommand> { vec!(
    ServerCommand::SetPosition(42, Position(50.0, 10.0, 25.0)),
    ServerCommand::SetOrientation(42, Orientation(0.0, 0.0)),
    ServerCommand::AddObject(42, Position(0.0, 0.0, 0.0), Orientation(0.0, 0.0), ObjectType::Player),
    ServerCommand::RemoveObject(42),
)}

fn show_examples(mut stream: TcpStream, playernum: i64) {
    let seconds = time::get_time().sec;
    println!("Received a connection from {:?} at time {:?} (player {:?}).", stream.peer_addr(), seconds, playernum);
    //writeln!(stream, "{}", &json::encode(&IncomingMessage{command: PlayerCommand::MoveForward(0.5), timestamp: 0}).unwrap()).unwrap();
    for cmd in example_servercommands().iter() {
        writeln!(stream, "{}", &json::encode(&OutgoingMessage{command: cmd.clone(), timestamp: seconds}).unwrap()).unwrap();
    }
    for &cmd in example_playercommands().iter() {
        writeln!(stream, "{}", &json::encode(&IncomingMessage{command: cmd, timestamp: seconds}).unwrap()).unwrap();
    }
}

fn interact_with_client(stream: TcpStream,
                        playernum: i64,
                        transmit_servctl: Sender<ServerControlMsg>) {
    println!("Player #{:?} joined ({:?}).", playernum, stream.peer_addr());
    let buffered = BufStream::new(stream.try_clone().unwrap());
    process_input_from_client(buffered, playernum, transmit_servctl);
}

fn process_input_from_client(stream: BufStream<TcpStream>,
                            playernum: i64,
                            transmit_servctl: Sender<ServerControlMsg>) {
    for line in stream.lines() {
        match line {
            Ok(line) => {
                match json::decode(&line) {
                    Ok(command) => { transmit_servctl.send(
                        ServerControlMsg::ProcessPlayerAction(playernum, command)
                    ).unwrap(); }
                    Err(e) => { println!("Bad input from player #{:?}: {:?} (ignoring)", playernum, e); }
                }
            }
            Err(e) => { println!("Some error occurred reading a line: {:?}", e); }
        }
    }
    transmit_servctl.send(ServerControlMsg::DisconnectPlayer(playernum)).unwrap();
}

fn send_initialization_to_client(stream: &mut TcpStream,
                                 playernum: i64,
                                 world: &HashMap<i64, GameObject>) -> io::Result<()> {
    try!(writeln!(stream, "{}", &json::encode(&ServerCommand::SetPlayerNumber(playernum)).unwrap()));
    try!(writeln!(stream, "{}", &json::encode(&ServerCommand::InitializeWorld(world.clone())).unwrap()));
    Ok(())
}

fn send_action_to_client(stream: &mut TcpStream, playernum: i64, action: &ServerCommand) -> io::Result<()> {
    println!("{}", format!("Sending {:?} to client {}", action, playernum));
    try!(writeln!(stream, "{}", &json::encode(action).unwrap()));
    Ok(())
}

fn get_player_mesh() -> Vec<f64> {
    vec!()
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
                ServerCommand::AddObject(playerid, newplayer.pos, newplayer.ori, ObjectType::Player)
            )).unwrap();
            entry.insert(newplayer)
        }
        Occupied(entry) => { entry.into_mut() }
    };
    player
}

fn apply_polar_movement(pos: Position, magnitude: f64, theta: f64) -> Position {
    let Position(x, y, z) = pos;
    Position(x + magnitude*theta.cos(), y, z + magnitude*theta.sin())
}

fn process_player_action(world: &mut HashMap<i64, GameObject>,
                         broadcast: Sender<ServerControlMsg>,
                         playerid: i64,
                         action: PlayerCommand) {
    let broadcast_location = |obj: &GameObject, i: i64| {
        broadcast.send(ServerControlMsg::BroadcastCommand(ServerCommand::SetPosition(i, obj.pos))).unwrap();
    };
    drop(get_player(world, playerid, broadcast.clone()));
    match action {
        PlayerCommand::MoveForward(delta) => {
            println!("Player #{:?} moves {:?} units forward", playerid, delta);
            let player = get_player(world, playerid, broadcast.clone());
            let Orientation(theta, _) = player.ori;
            player.pos = apply_polar_movement(player.pos, delta, -theta + PI/2.0);
            println!("P#{:?} pos: {:?}", playerid, player.pos);
            broadcast_location(player, playerid);
        }
        PlayerCommand::MoveSideways(delta) => {
            println!("Player #{:?} moves {:?} units to their right", playerid, delta);
            let player = get_player(world, playerid, broadcast.clone());
            let Orientation(theta, _) = player.ori;
            player.pos = apply_polar_movement(player.pos, delta, -theta);
            println!("P#{:?} pos: {:?}", playerid, player.pos);
            broadcast_location(player, playerid);
        }
        PlayerCommand::MoveUp(delta) => {
            println!("Player #{:?} moves {:?} units up", playerid, delta);
            let player = get_player(world, playerid, broadcast.clone());
            player.pos = player.pos + Position(0.0, delta, 0.0);
            println!("P#{:?} pos: {:?}", playerid, player.pos);
            broadcast_location(player, playerid);
        }
        PlayerCommand::RotateCamera(Orientation(theta, phi)) => {
            println!("Player #{:?} rotates by ({:?}, {:?})", playerid, theta, phi);
            let player = &mut get_player(world, playerid, broadcast.clone());
            player.ori = player.ori + Orientation(theta, phi);
            println!("P#{:?} ori: {:?}", playerid, player.ori);
            broadcast.send(ServerControlMsg::BroadcastCommand(
                ServerCommand::SetOrientation(playerid, player.ori)
            )).unwrap();
        }
        PlayerCommand::Shoot => { println!("Player #{:?} shoots", playerid); }
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

fn listener_loop(sender: Sender<ServerControlMsg>) {
    let listener = TcpListener::bind(("0.0.0.0", 51701)).unwrap(); //large number for port chosen pseudorandomly
    for stream in listener.incoming() {
        match stream {
            Err(e) => { println!("Error accepting incoming connection: {:?}", e); },
            Ok(stream) => {
                sender.send(ServerControlMsg::StartConnection(stream)).unwrap();
            },
        }
    }
}

// contains some code adapted from example at http://doc.rust-lang.org/std/net/struct.TcpListener.html
fn main() {
    println!("current time: {:?}", time::get_time());
    println!("address of dWorldCreate: {:p}", &ode_bindgen::dWorldCreate);
    let mut world = HashMap::<i64, GameObject>::new();
    let mut playernum: i64 = 0;

    let (transmit_servctl, receive_servctl) = channel();
    {
        let tx = transmit_servctl.clone();
        spawn(move || { listener_loop(tx); });
    }

    let mut connections = HashMap::<i64, TcpStream>::new();

    for servctl in receive_servctl.iter() {
        match servctl {
            ServerControlMsg::StartConnection(mut stream) => {
                playernum += 1;
                connections.insert(playernum, stream.try_clone().unwrap());
                send_initialization_to_client(&mut stream, playernum, &world).unwrap();
                {
                    let tx = transmit_servctl.clone();
                    spawn(move || { interact_with_client(stream, playernum, tx); });
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
                process_player_action(&mut world, transmit_servctl.clone(), pid, action);
            }
            ServerControlMsg::DisconnectPlayer(pid) => {
                connections.remove(&pid);
                transmit_servctl.send(ServerControlMsg::BroadcastCommand(
                    ServerCommand::RemoveObject(pid)
                )).unwrap();
            }
        }
    }
}
