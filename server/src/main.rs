extern crate serialize;
use serialize::json;
use std::io::{TcpListener, TcpStream};
use std::io::{Acceptor, Listener};

pub type PlayerId = int;

#[deriving(Encodable, Decodable)]
pub enum Command {
    MoveForward(PlayerId, f64),
    MoveSideways(PlayerId, f64),
}

#[deriving(Encodable, Decodable)]
pub struct Message {
    command: Command,
    timestamp: u64,
}
    

// contains some code adapted from example at http://doc.rust-lang.org/std/io/net/tcp/struct.TcpListener.html

fn handle_client(mut stream: TcpStream) {
    println!("Received a connection from {}.", stream.peer_name());
    stream.write_line("Hello, world!");
    stream.write_line(json::encode(&Message { command: MoveForward(0, 0.5), timestamp: 0 }).as_slice());
}

fn main() {
    let listener = TcpListener::bind("127.0.0.1:51701"); //large number for port chosen pseudorandomly

    let mut acceptor = listener.listen();
    
    for stream in acceptor.incoming() {
        match stream {
            Err(e) => { println!("Error accepting incoming connection: {}", e) }
            Ok(stream) => spawn(proc() {
                handle_client(stream)
            })
        }
    }
    println!("Hello, world!");
}
