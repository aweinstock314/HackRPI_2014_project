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
        Object obj = JSONValue.parse(jsonString);
        JSONObject jsobj = (JSONObject)obj;
        JSONObject cmd = (JSONObject)jsobj.get(1);
        String cmdType = cmd.get("variant");
        JSONArray fields = (JSONArray)cmd.get("fields");
        int i = fields.get(0);
        if(cmdType == "SetPosition") {
            JSONObject posData = (JSONObject)files.get(1);
            double x = posData.get("_field0");
            double y = posData.get("_field1");
            double z = posData.get("_field2");
            go.actors.get(i).setPosition(x,y,z);
        } else if(cmdType == "SetOrientation") {
            JSONObject orData = (JSONObject)files.get(1);
            double th = orData.get("_field0");
            double ph = orData.get("_field1");
            go.actors.get(i).setOrientation(th,ph);
        } else if(cmdType == "AddObject") {
            JSONObject posData = (JSONObject)files.get(1);
            double x = posData.get("_field0");
            double y = posData.get("_field1");
            double z = posData.get("_field2");
            JSONObject orData = (JSONObject)files.get(1);
            double th = orData.get("_field0");
            double ph = orData.get("_field1");
            go.actors.put(i,new DrawObject(x,y,z,th,ph));
        } else if(cmdType == "RemoveObject") {
            go.actors.remove(i);
        }
    }
}
