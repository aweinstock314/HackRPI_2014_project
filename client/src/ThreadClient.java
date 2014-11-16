package client;
import java.io.*;
import java.net.*;
import org.json.simple.*;

public class ThreadClient {
    private BufferedReader in;
    private PrintWriter out;
    private Socket sock;

    public ThreadClient(String hostname, int portnum) {
        try {
            this.sock = new Socket(hostname, portnum);
            this.out = new PrintWriter(sock.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Can't find given host: " + hostname);
        } catch (IOException e) {
            System.err.println("Couldn't get connection I/O");
        }
    }

    public PrintWriter getWriter() {
        return out;
    }

    public BufferedReader getReader() {
        return in;
    }

    public static void main(String[] args) {
        ThreadClient tc = new ThreadClient(args[0], 51701);
        BufferedReader in = tc.getReader();
        try {
            while(true) {
                String initialString = in.readLine();
                //Object obj = JSONValue.parse(initialString);
                //JSONArray array = (JSONArray)obj;
                System.out.println(initialString);
            }
        } catch(IOException io) {
            System.err.println("Read failed");
        }
    }
}
