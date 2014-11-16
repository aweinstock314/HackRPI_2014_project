package client;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.simple.*;

public class ServerSyncer extends TimerTask {

    private BufferedReader in;
    private GameObject go;
    private CameraHandler ch;

    public ServerSyncer(BufferedReader br, GameObject go, CameraHandler ch) {
        this.in = br;
        this.go = go;
        this.ch = ch;
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
        try {
            System.out.println(jsonString);
            Object obj = JSONValue.parse(jsonString);
            JSONObject jsobj = (JSONObject)obj;
            if(obj == null) return;
            String cmdType = (String)jsobj.get("variant");
            JSONArray fields = (JSONArray)jsobj.get("fields");
            long i = (Long)fields.get(0);
                System.out.println(cmdType);
            if(cmdType.equals("SetPosition")) {
                JSONObject posData = (JSONObject)fields.get(1);
                float x = ((Number)posData.get("_field0")).floatValue();
                float y = ((Number)posData.get("_field1")).floatValue();
                float z = ((Number)posData.get("_field2")).floatValue();
                go.actors.get(i).setPosition(x,y,z);
            } else if(cmdType.equals("SetOrientation")) {
                JSONObject orData = (JSONObject)fields.get(1);
                float th = ((Number)orData.get("_field0")).floatValue();
                float ph = ((Number)orData.get("_field1")).floatValue();
                go.actors.get(i).setOrientation(th,ph);
            } else if(cmdType.equals("AddObject")) {
                System.out.println(cmdType);
                JSONObject posData = (JSONObject)fields.get(1);
                float x = ((Number)posData.get("_field0")).floatValue();
                float y = ((Number)posData.get("_field1")).floatValue();
                float z = ((Number)posData.get("_field2")).floatValue();
                JSONObject orData = (JSONObject)fields.get(2);
                float th = ((Number)orData.get("_field0")).floatValue();
                float ph = ((Number)orData.get("_field1")).floatValue();
                String type = (String)fields.get(3);
                DrawObject newObj = new DrawObject(x,y,z,th,ph,type,(i==-1),ch);
                go.actors.put(i,newObj);
            } else if(cmdType.equals("RemoveObject")){
                go.actors.remove(i);
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not parse JSON");
        }
    }
}
