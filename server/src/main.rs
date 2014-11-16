extern crate serialize;
extern crate time;
use serialize::json;
use std::io::{TcpListener, TcpStream, BufferedStream};
use std::io::{Acceptor, Listener};
use std::collections::HashMap;

// hack around cargo not respecting "proper" link attributes
#[link(name = "ode")] extern {}
mod ode_bindgen;

#[deriving(Encodable, Decodable)]
//pub struct Position { x: f64, y: f64, z: f64 }
pub struct Position (f64, f64, f64);

#[deriving(Encodable, Decodable)]
//pub struct Orientation { theta: f64, phi: f64 }
pub struct Orientation (f64, f64);

#[deriving(Encodable, Decodable)]
pub enum PlayerCommand {
    MoveForward(f64),
    MoveSideways(f64),
    RotateCamera(Orientation),
    Shoot,
}

#[deriving(Encodable, Decodable)]
pub enum ObjectType {
    Floor,
    Obstacle(int),
    Player,
    Bullet,
}

#[deriving(Encodable, Decodable)]
pub enum ServerCommand {
    SetPosition(int, Position),
    SetOrientation(int, Orientation),
    AddObject(int, Position, Orientation, ObjectType),
    RemoveObject(int),
}

#[deriving(Encodable, Decodable)]
pub struct IncomingMessage {
    command: PlayerCommand,
    timestamp: i64,
}

#[deriving(Encodable, Decodable)]
pub struct OutgoingMessage {
    command: ServerCommand,
    timestamp: i64,
}

pub struct GameObject {
    pos: Position,
    ori: Orientation,
    mesh: Vec<f64>,
}

/*fn createPlayer(&mut world: &mut HashMap<int, GameObject>) {
}*/

fn example_playercommands() -> Vec<PlayerCommand> { vec!(
    MoveForward(0.5),
    MoveSideways(0.6),
    RotateCamera(Orientation(-1.5, 0.2)),
    Shoot,
)}

fn example_servercommands() -> Vec<ServerCommand> { vec!(
    SetPosition(42, Position(50.0, 10.0, 25.0)),
    SetOrientation(42, Orientation(0.0, 0.0)),
    AddObject(42, Position(0.0, 0.0, 0.0), Orientation(0.0, 0.0), Player),
    RemoveObject(42),
)}

fn show_examples(mut stream: TcpStream, playernum: int, transmit_playmove: Sender<(int, PlayerCommand)>) {
    let seconds = time::get_time().sec;
    println!("Received a connection from {} at time {} (player {}).", stream.peer_name(), seconds, playernum);
    //stream.write_line(json::encode(&IncomingMessage{command: MoveForward(0.5), timestamp: 0}).as_slice());
    for &cmd in example_servercommands().iter() {
        stream.write_line(json::encode(&OutgoingMessage{command: cmd, timestamp: seconds}).as_slice());
    }
    for &cmd in example_playercommands().iter() {
        stream.write_line(json::encode(&IncomingMessage{command: cmd, timestamp: seconds}).as_slice());
    }
}

/*fn interact_with_client(mut stream: TcpStream) {
    let mut buffered = BufferedStream::new(stream);
    send_world_creation(&mut buffered);
    loop {
        send_and_receive_updates(&mut buffered);
    }
}
*/

fn manage_world(mut world: HashMap<int, GameObject>,
                broadcast: Sender<ServerCommand>,
                player_moves: Receiver<(int, PlayerCommand)>) {
    loop {
        let (playerid, action) = player_moves.recv();
        match action {
            MoveForward(delta) => {
                println!("Player #{} moves {} units forward", playerid, delta);
            }
            MoveSideways(delta) => {
                println!("Player #{} moves {} units to their right", playerid, delta);
            }
            RotateCamera(Orientation(theta, phi)) => {
                println!("Player #{} rotates by ({}, {})", playerid, theta, phi);
            }
            Shoot => { println!("Player #{} shoots", playerid); }
        }
    }
}

// contains some code adapted from example at http://doc.rust-lang.org/std/io/net/tcp/struct.TcpListener.html
fn main() {
    println!("current time: {}", time::get_time());
    println!("address of dWorldCreate: {:p}", &ode_bindgen::dWorldCreate);
    //let listener = TcpListener::bind("127.0.0.1:51701"); //large number for port chosen pseudorandomly
    let listener = TcpListener::bind("0.0.0.0:51701"); //large number for port chosen pseudorandomly

    let mut world = HashMap::<int, GameObject>::new();
    let mut playernum: int = 0;

    let (transmit_broadcast, receive_broadcast) = channel();
    let (transmit_playmove, receive_playmove) = channel();

    spawn(proc() { manage_world(world, transmit_broadcast, receive_playmove); });

    let mut acceptor = listener.listen();
    
    for stream in acceptor.incoming() {
        match stream {
            Err(e) => { println!("Error accepting incoming connection: {}", e); return; }
            Ok(stream) => {
                playernum += 1;
                let tpm = transmit_playmove.clone();
                    spawn(proc() {
                    show_examples(stream, playernum.clone(), tpm);
                })
            }
        }
    }
}
