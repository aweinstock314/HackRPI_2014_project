extern crate serialize;
use serialize::json;
use std::io::{TcpListener, TcpStream};
use std::io::{Acceptor, Listener};


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
    timestamp: u64,
}

#[deriving(Encodable, Decodable)]
pub struct OutgoingMessage {
    command: ServerCommand,
    timestamp: u64,
}

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

fn show_examples(mut stream: TcpStream) {
    println!("Received a connection from {}.", stream.peer_name());
    //stream.write_line(json::encode(&IncomingMessage{command: MoveForward(0.5), timestamp: 0}).as_slice());
    for &cmd in example_servercommands().iter() {
        stream.write_line(json::encode(&OutgoingMessage{command: cmd, timestamp: 0}).as_slice());
    }
    for &cmd in example_playercommands().iter() {
        stream.write_line(json::encode(&IncomingMessage{command: cmd, timestamp: 0}).as_slice());
    }
}

// contains some code adapted from example at http://doc.rust-lang.org/std/io/net/tcp/struct.TcpListener.html
fn main() {
    //let listener = TcpListener::bind("127.0.0.1:51701"); //large number for port chosen pseudorandomly
    let listener = TcpListener::bind("0.0.0.0:51701"); //large number for port chosen pseudorandomly

    let mut acceptor = listener.listen();
    
    for stream in acceptor.incoming() {
        match stream {
            Err(e) => { println!("Error accepting incoming connection: {}", e) }
            Ok(stream) => spawn(proc() {
                show_examples(stream)
            })
        }
    }
}
