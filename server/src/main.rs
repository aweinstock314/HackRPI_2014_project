use std::io::{TcpListener, TcpStream};
use std::io::{Acceptor, Listener};

// contains some code adapted from example at http://doc.rust-lang.org/std/io/net/tcp/struct.TcpListener.html

fn handle_client(mut stream: TcpStream) {
    stream.write_line("Hello, world!");
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
