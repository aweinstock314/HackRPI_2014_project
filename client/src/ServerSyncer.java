package client;
import java.io.*;

public class ServerSyncer implements TimerTask {

    private BufferedReader in;
    private GameObject go;

    public ServerSyncer(BufferedReader br, GameObject go) {
        this.in = br;
        this.go = go;
        try {
            while(true) {
                String initialString = in.readline();
                parse(initialString);
            }
        } catch(IOException e) {
            System.err.println("Reading failed");
        }
    }

    private void parse(String jsonString) {


    }
}
