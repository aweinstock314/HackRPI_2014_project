package client;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.simple.*;

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
        String cmdType = (String)cmd.get("variant");
        JSONArray fields = (JSONArray)cmd.get("fields");
        int i = (Integer)fields.get(0);
        if(cmdType == "SetPosition") {
            JSONObject posData = (JSONObject)fields.get(1);
            double x = (Double)posData.get("_field0");
            double y = (Double)posData.get("_field1");
            double z = (Double)posData.get("_field2");
            go.actors.get(i).setPosition(x,y,z);
        } else if(cmdType == "SetOrientation") {
            JSONObject orData = (JSONObject)fields.get(1);
            double th = (Double)orData.get("_field0");
            double ph = (Double)orData.get("_field1");
            go.actors.get(i).setOrientation(th,ph);
        } else if(cmdType == "AddObject") {
            JSONObject posData = (JSONObject)fields.get(1);
            double x = (Double)posData.get("_field0");
            double y = (Double)posData.get("_field1");
            double z = (Double)posData.get("_field2");
            JSONObject orData = (JSONObject)fields.get(1);
            double th = (Double)orData.get("_field0");
            double ph = (Double)orData.get("_field1");
            String type = (String)fields.get(2);
            go.actors.put(i,new DrawObject(x,y,z,th,ph,type));
        } else if(cmdType == "RemoveObject") {
            go.actors.remove(i);
        }
    }
}
