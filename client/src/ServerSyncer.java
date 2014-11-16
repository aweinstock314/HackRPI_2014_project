package client;
import java.io.*;
import java.net.*;
import java.util.*;

public class ServerSyncer extends TimerTask {

    private BufferedReader in;
    private GameObject go;

    public ServerSyncer(BufferedReader br, GameObject go) {
        this.in = br;
        this.go = go;
    }
    public void run() {
        try {
            while(true) {
                String initialString = in.readLine();
                parse(initialString);
            }
        } catch(IOException e) {
            System.err.println("Reading failed");
        }
    }

    private void parse(String jsonString) {


    }
}
