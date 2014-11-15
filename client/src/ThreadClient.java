package client;
import java.io.*;
import java.net.*;
import org.json.simple.JSONObject;

public class ThreadClient {
    private BufferedReader in;
    private PrintWriter out;
    private Socket sock;

    public ThreadClient(String hostname, int portnum) {
        try {
            this.sock = new Socket(hostname, portnum);
            this.out = new PrintWriter(sock.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            while(true) {
                String initialString = in.readLine();
                System.out.println(initialString);
            }
        } catch (UnknownHostException e) {
            System.err.println("Can't find given host: " + hostname);
        } catch (IOException e) {
            System.err.println("Couldn't get connection I/O");
        }
    }
    public static void main(String[] args) {
        ThreadClient tc = new ThreadClient(args[0], 51701);
    }
}
