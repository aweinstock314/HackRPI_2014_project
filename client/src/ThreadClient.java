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
            System.exit(0);
        }
    }

    public PrintWriter getWriter() {
        return out;
    }

    public BufferedReader getReader() {
        return in;
    }

    public static void main(String[] args) {
        ThreadClient tc = new ThreadClient("129.161.91.206",51701);
        BufferedReader in = tc.getReader();
        PrintWriter out = tc.getWriter();
        while(true) {
            out.println("What are you telling me?");
            try {
            String s = in.readLine();
            System.out.println(s);
            out.println("You told me: " + s);
            } catch(Exception e) {} //I'm too tired to care
        }
    }
}
