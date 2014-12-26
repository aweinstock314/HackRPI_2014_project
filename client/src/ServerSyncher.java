package client;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.simple.*;

public class ServerSyncher implements Runnable {

    private BufferedReader reader;
    private GameWorld world;
    private CameraHandler ch;

    public ServerSyncher(Socket s, GameWorld w, CameraHandler c) throws IOException {
        constructorAux(new BufferedReader(new InputStreamReader(s.getInputStream())), w, c);
    }
    public ServerSyncher(BufferedReader br, GameWorld w, CameraHandler c) {
        constructorAux(br, w, c);
    }

    protected void constructorAux(BufferedReader br, GameWorld w, CameraHandler c) {
        reader = br;
        world = w;
        ch = c;
    }

    @Override
    public void run() {
        while(true) {
            try {
                String line = reader.readLine();
                if(line == null) { break; }
                parseAndUpdateWorld(line);
            }
            catch(Exception e) { e.printStackTrace(); }
        }
    }

    private static JSONArray getModel(String type) {
        // TODO: model loading based on type of object
        try {
            System.out.println(type);
            if("Player".equals(type)) {
                return (JSONArray)JSONValue.parse(new FileReader("player_model.json"));
            }
            else { return (JSONArray)JSONValue.parse(new FileReader("unit_sphere.json")); }
        }
        catch(Exception e) { e.printStackTrace(); return null; }
    }

    private void parseAndUpdateWorld(String jsonString) {
        try {
            synchronized(world) {
                System.out.printf("ServerSyncher: parsing \"%s\"\n", jsonString);
                Object obj = JSONValue.parse(jsonString);
                JSONObject jsobj = (JSONObject)obj;
                if(obj == null) return;
                String cmdType = (String)jsobj.get("variant");
                JSONArray fields = (JSONArray)jsobj.get("fields");
                long i = (Long)fields.get(0);
                //System.out.println(cmdType);
                if(cmdType.equals("SetPosition")) {
                    JSONObject posData = (JSONObject)fields.get(1);
                    float x = ((Number)posData.get("_field0")).floatValue();
                    float y = ((Number)posData.get("_field1")).floatValue();
                    float z = ((Number)posData.get("_field2")).floatValue();
                    world.actors.get(i).setPosition(x,y,z);
                } else if(cmdType.equals("SetOrientation")) {
                    JSONObject orData = (JSONObject)fields.get(1);
                    float th = ((Number)orData.get("_field0")).floatValue();
                    float ph = ((Number)orData.get("_field1")).floatValue();
                    world.actors.get(i).setOrientation(th,ph);
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
                    DrawObject newObj = new DrawObject(x,y,z,th,ph,type,getModel(type));
                    world.actors.put(i,newObj);
                } else if(cmdType.equals("RemoveObject")){
                    world.actors.remove(i);
                } else if(cmdType.equals("SetPlayerNumber")) {
                    world.playernum = i;
                }
            }
        }
        catch(NullPointerException npe) {
            // TODO: initialize each client with all the other clients the server is handling (server-side)
            System.err.println("Error: attempting to access an object that does was not added to this client.");
            npe.printStackTrace();
        } 
        catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not parse JSON");
        }
    }
}
