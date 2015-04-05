#![feature(old_io)]
extern crate libc;
extern crate rustc_serialize;
extern crate time;
use rustc_serialize::json;
use std::old_io::{TcpListener, TcpStream, BufferedStream};
use std::old_io::{Acceptor, Listener, Writer, BufferPrelude};
use std::collections::HashMap;
use std::collections::hash_map::Entry::{Vacant, Occupied};
use std::sync::{Mutex, Arc};
use std::ops::Add;
use std::thread::spawn;
use std::sync::mpsc::{channel, Sender, Receiver};

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

#[derive(RustcEncodable, RustcDecodable, Clone, Copy)]
pub enum ObjectType {
    Floor,
    Obstacle(i64),
    Player,
    Bullet,
}

#[derive(RustcEncodable, RustcDecodable, Clone)]
pub enum ServerCommand {
    SetPosition(i64, Position),
    SetOrientation(i64, Orientation),
    AddObject(i64, Position, Orientation, ObjectType),
    RemoveObject(i64),
    SetPlayerNumber(i64),
    InitializeWorld(HashMap<i64, GameObject>),
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

#[derive(RustcEncodable, RustcDecodable, Clone)]
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

fn show_examples(mut stream: TcpStream, playernum: i64, transmit_playmove: Sender<(i64, PlayerCommand)>) {
    let seconds = time::get_time().sec;
    println!("Received a connection from {:?} at time {:?} (player {:?}).", stream.peer_name(), seconds, playernum);
    //stream.write_line(&json::encode(&IncomingMessage{command: PlayerCommand::MoveForward(0.5), timestamp: 0}).unwrap());
    for cmd in example_servercommands().iter() {
        stream.write_line(&json::encode(&OutgoingMessage{command: cmd.clone(), timestamp: seconds}).unwrap());
    }
    for &cmd in example_playercommands().iter() {
        stream.write_line(&json::encode(&IncomingMessage{command: cmd, timestamp: seconds}).unwrap());
    }
}

fn interact_with_client(mut stream: TcpStream,
                        playernum: i64,
                        receive_broadcast: Receiver<ServerCommand>,
                        transmit_playmove: Sender<(i64, PlayerCommand)>,
                        world: Arc<Mutex<HashMap<i64, GameObject>>>) {
    println!("Player #{:?} joined ({:?}).", playernum, stream.peer_name());
    let buffered = BufferedStream::new(stream.clone());
    spawn(move || { process_input_from_client(buffered, playernum, transmit_playmove) });
    process_output_to_client(stream, playernum, receive_broadcast, world);
}

fn process_input_from_client(mut stream: BufferedStream<TcpStream>,
                            playernum: i64,
                            transmit_playmove: Sender<(i64, PlayerCommand)>) {
    for line in stream.lines() {
        match line {
            Ok(line) => {
                match json::decode(&line) {
                    Ok(command) => { transmit_playmove.send((playernum, command)); }
                    Err(e) => { println!("Bad input from player #{:?}: {:?} (ignoring)", playernum, e); }
                }
            }
            Err(e) => { println!("Some error occurred reading a line: {:?}", e); }
        }
    }
}

fn process_output_to_client(mut stream: TcpStream,
                            playernum: i64,
                            receive_broadcast: Receiver<ServerCommand>,
                            world: Arc<Mutex<HashMap<i64, GameObject>>>) {
    stream.write_line(&json::encode(&ServerCommand::SetPlayerNumber(playernum)).unwrap());
    stream.write_line(&json::encode(&ServerCommand::InitializeWorld(world.lock().unwrap().clone())).unwrap());
    for action in receive_broadcast.iter() {
        stream.write_line(&json::encode(&action).unwrap());
    }
}

fn get_player_mesh() -> Vec<f64> {
    vec!()
}

fn get_player(world: &mut HashMap<i64, GameObject>,
                playerid: i64,
                broadcast: Sender<ServerCommand>) -> &mut GameObject {
    let player = match world.entry(playerid) {
        Vacant(entry) => {
            let newplayer = GameObject {
                pos: Position(0.0, 0.0, 0.0),
                ori: Orientation(0.0, 0.0),
                obj_type: ObjectType::Player,
            };
            broadcast.send(ServerCommand::AddObject(playerid, newplayer.pos, newplayer.ori, ObjectType::Player));
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

fn manage_world(world: Arc<Mutex<HashMap<i64, GameObject>>>,
                broadcast: Sender<ServerCommand>,
                player_moves: Receiver<(i64, PlayerCommand)>) {
    let broadcast_location = |obj: &GameObject, i: i64| {
        broadcast.send(ServerCommand::SetPosition(i, obj.pos));
    };
    for (playerid, action) in player_moves.iter() {
        drop(get_player(&mut *world.lock().unwrap(), playerid, broadcast.clone()));
        match action {
            PlayerCommand::MoveForward(delta) => {
                println!("Player #{:?} moves {:?} units forward", playerid, delta);
                let mut wrld = world.lock().unwrap();
                let player = get_player(&mut *wrld, playerid, broadcast.clone());
                let Orientation(theta, _) = player.ori;
                player.pos = apply_polar_movement(player.pos, delta, -theta + PI/2.0);
                println!("P#{:?} pos: {:?}", playerid, player.pos);
                broadcast_location(player, playerid);
            }
            PlayerCommand::MoveSideways(delta) => {
                println!("Player #{:?} moves {:?} units to their right", playerid, delta);
                let mut wrld = world.lock().unwrap();
                let player = get_player(&mut *wrld, playerid, broadcast.clone());
                let Orientation(theta, _) = player.ori;
                player.pos = apply_polar_movement(player.pos, delta, -theta);
                println!("P#{:?} pos: {:?}", playerid, player.pos);
                broadcast_location(player, playerid);
            }
            PlayerCommand::MoveUp(delta) => {
                println!("Player #{:?} moves {:?} units up", playerid, delta);
                let mut wrld = world.lock().unwrap();
                let player = get_player(&mut *wrld, playerid, broadcast.clone());
                let Orientation(theta, _) = player.ori;
                player.pos = player.pos + Position(0.0, delta, 0.0);
                println!("P#{:?} pos: {:?}", playerid, player.pos);
                broadcast_location(player, playerid);
            }
            PlayerCommand::RotateCamera(Orientation(theta, phi)) => {
                println!("Player #{:?} rotates by ({:?}, {:?})", playerid, theta, phi);
                let mut wrld = world.lock().unwrap();
                let player = &mut get_player(&mut *wrld, playerid, broadcast.clone());
                player.ori = player.ori + Orientation(theta, phi);
                println!("P#{:?} ori: {:?}", playerid, player.ori);
                broadcast.send(ServerCommand::SetOrientation(playerid, player.ori));
            }
            PlayerCommand::Shoot => { println!("Player #{:?} shoots", playerid); }
        }
    }
}

/*pub struct ReceiverMultiplexer<T: Send+Clone> {
    receiver: Receiver<T>,
    transmitters: Vec<Sender<T>>
}

impl<T: Send+Clone> ReceiverMultiplexer<T> {
    fn new(r: Receiver<T>) -> ReceiverMultiplexer<T> {
        ReceiverMultiplexer { receiver: r, transmitters: vec!() }
    }
    fn add_transmitter(&mut self, s: Sender<T>) {
        self.transmitters.push(s);
    }
    fn rebroadcast(&mut self) {
        for msg in self.receiver.iter() {
            for transmitter in self.transmitters.iter() {
                transmitter.send(msg.clone());
            }
        }
    }
}*/

fn rebroadcast_transmitter<T: Send+Clone>(r: Receiver<T>, ts: Arc<Mutex<Vec<Sender<T>>>>)
{
    for msg in r.iter() {
        let val = ts.lock().unwrap();
        for t in val.iter() {
            t.send(msg.clone());
        }
        drop(val);
    }
}

fn odeMainTest() {
    // transcribed from ODE's "demo_buggy" example
    unsafe {
        ode_bindgen::dInitODE();
        let world = ode_bindgen::dWorldCreate();
        let space = ode_bindgen::dHashSpaceCreate(0 as *mut ode_bindgen::Struct_dxSpace);
        let ground = ode_bindgen::dCreatePlane(space, 0.0, 1.0, 0.0, 0.0);
        ode_bindgen::dCloseODE();
    }
}

// contains some code adapted from example at http://doc.rust-lang.org/std/io/net/tcp/struct.TcpListener.html
fn main() {
    println!("current time: {:?}", time::get_time());
    println!("address of dWorldCreate: {:p}", &ode_bindgen::dWorldCreate);
    //let listener = TcpListener::bind("127.0.0.1:51701"); //large number for port chosen pseudorandomly
    let listener = TcpListener::bind("0.0.0.0:51701"); //large number for port chosen pseudorandomly

    let world = Arc::new(Mutex::new(HashMap::<i64, GameObject>::new()));
    let mut playernum: i64 = 0;

    let (transmit_broadcast, receive_broadcast_precursor) = channel();
    let (transmit_playmove, receive_playmove) = channel();

    //let mut receive_broadcast = ReceiverMultiplexer::new(receive_broadcast_precursor);
    let transmitters = Arc::new(Mutex::new(vec!()));
    let transmitters2 = transmitters.clone();
    spawn(move || { rebroadcast_transmitter(receive_broadcast_precursor, transmitters2); });

    {
        let wrld = world.clone();
        spawn(move || { manage_world(wrld, transmit_broadcast, receive_playmove); });
    }
    //spawn(move || { receive_broadcast.rebroadcast(); });
    

    let mut acceptor = listener.listen();
    
    for stream in acceptor.incoming() {
        match stream {
            Err(e) => { println!("Error accepting incoming connection: {:?}", e); return; }
            Ok(stream) => {
                playernum += 1;
                let tpm = transmit_playmove.clone();
                let (tx, rx) = channel();
                //receive_broadcast.add_transmitter(tx);
                {
                    let mut val = transmitters.lock().unwrap();
                    val.push(tx);
                    drop(val);
                }
                //let rbc = receive_broadcast.clone();
                //spawn(move || { show_examples(stream, playernum.clone(), tpm); });
                {
                    let wrld = world.clone();
                    spawn(move || { interact_with_client(stream, playernum, rx, tpm, wrld); });
                }
            }
        }
    }
}
