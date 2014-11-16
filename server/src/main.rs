extern crate serialize;
extern crate time;
use serialize::json;
use std::io::{TcpListener, TcpStream, BufferedStream};
use std::io::{Acceptor, Listener};
use std::collections::HashMap;
use std::collections::hash_map::{Vacant, Occupied};
use std::sync::{Mutex, Arc};

// hack around cargo not respecting "proper" link attributes
#[link(name = "ode")] extern {}
mod ode_bindgen;

#[deriving(Encodable, Decodable, Clone)]
//pub struct Position { x: f64, y: f64, z: f64 }
pub struct Position (f64, f64, f64);

#[deriving(Encodable, Decodable, Clone)]
//pub struct Orientation { theta: f64, phi: f64 }
pub struct Orientation (f64, f64);

#[deriving(Encodable, Decodable, Clone)]
pub enum PlayerCommand {
    MoveForward(f64),
    MoveSideways(f64),
    RotateCamera(Orientation),
    Shoot,
}

#[deriving(Encodable, Decodable, Clone)]
pub enum ObjectType {
    Floor,
    Obstacle(int),
    Player,
    Bullet,
}

#[deriving(Encodable, Decodable, Clone)]
pub enum ServerCommand {
    SetPosition(int, Position),
    SetOrientation(int, Orientation),
    AddObject(int, Position, Orientation, ObjectType),
    RemoveObject(int),
}

#[deriving(Encodable, Decodable, Clone)]
pub struct IncomingMessage {
    command: PlayerCommand,
    timestamp: i64,
}

#[deriving(Encodable, Decodable, Clone)]
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

fn interact_with_client(mut stream: TcpStream,
                        playernum: int,
                        receive_broadcast: Receiver<ServerCommand>,
                        transmit_playmove: Sender<(int, PlayerCommand)>) {
    println!("Player #{} joined ({}).", playernum, stream.peer_name());
    let mut buffered = BufferedStream::new(stream.clone());
    spawn(proc() { process_input_from_client(buffered, playernum, transmit_playmove) });
    process_output_to_client(stream, playernum, receive_broadcast);
    /*//send_world_creation(&mut buffered);
    loop {
        //send_and_receive_updates(&mut buffered);
    }*/
}

fn process_input_from_client(mut stream: BufferedStream<TcpStream>,
                            playernum: int,
                            transmit_playmove: Sender<(int, PlayerCommand)>) {
    for line in stream.lines() {
        match line {
            Ok(line) => {
                match json::decode(line.as_slice()) {
                    Ok(command) => { transmit_playmove.send((playernum, command)); }
                    Err(e) => { println!("Bad input from player #{}: {} (ignoring)", playernum, e); }
                }
            }
            Err(e) => { println!("Some error occurred reading a line: {}", e); }
        }
    }
}

fn process_output_to_client(mut stream: TcpStream,
                            playernum: int,
                            receive_broadcast: Receiver<ServerCommand>) {
    for action in receive_broadcast.iter() {
        stream.write_line(json::encode(&action).as_slice());
    }
}

fn get_player_mesh() -> Vec<f64> {
    vec!()
}

fn get_player(world: &mut HashMap<int, GameObject>,
                playerid: int,
                broadcast: Sender<ServerCommand>) -> &mut GameObject {
    let player = match world.entry(playerid) {
        Vacant(entry) => {
            let newplayer = GameObject {
                pos: Position(0.0, 0.0, 0.0),
                ori: Orientation(0.0, 0.0),
                mesh: get_player_mesh()
            };
            broadcast.send(AddObject(playerid, newplayer.pos, newplayer.ori, Player));
            entry.set(newplayer)
        }
        Occupied(entry) => { entry.into_mut() }
    };
    player
}

fn manage_world(mut world: HashMap<int, GameObject>,
                broadcast: Sender<ServerCommand>,
                player_moves: Receiver<(int, PlayerCommand)>) {
    for (playerid, action) in player_moves.iter() {
        drop(get_player(&mut world, playerid, broadcast.clone()));
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
        let mut val = ts.lock();
        for t in val.iter() {
            t.send(msg.clone());
        }
        drop(val);
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

    let (transmit_broadcast, receive_broadcast_precursor) = channel();
    let (transmit_playmove, receive_playmove) = channel();

    //let mut receive_broadcast = ReceiverMultiplexer::new(receive_broadcast_precursor);
    let transmitters = Arc::new(Mutex::new(vec!()));
    let transmitters2 = transmitters.clone();
    spawn(proc() { rebroadcast_transmitter(receive_broadcast_precursor, transmitters2); });

    spawn(proc() { manage_world(world, transmit_broadcast, receive_playmove); });
    //spawn(proc() { receive_broadcast.rebroadcast(); });
    

    let mut acceptor = listener.listen();
    
    for stream in acceptor.incoming() {
        match stream {
            Err(e) => { println!("Error accepting incoming connection: {}", e); return; }
            Ok(stream) => {
                playernum += 1;
                let tpm = transmit_playmove.clone();
                let (tx, rx) = channel();
                //receive_broadcast.add_transmitter(tx);
                {
                    let mut val = transmitters.lock();
                    val.push(tx);
                    drop(val);
                }
                //let rbc = receive_broadcast.clone();
                //spawn(proc() { show_examples(stream, playernum.clone(), tpm); });
                spawn(proc() { interact_with_client(stream, playernum, rx, tpm); });
            }
        }
    }
}
