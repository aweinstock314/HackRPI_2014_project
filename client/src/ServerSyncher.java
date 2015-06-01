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
        return ModelCache.get(type);
    }

    private void setPosition(long i, JSONObject posData) {
        float x = ((Number)posData.get("_field0")).floatValue();
        float y = ((Number)posData.get("_field1")).floatValue();
        float z = ((Number)posData.get("_field2")).floatValue();
        world.actors.get(i).setPosition(x,y,z);
    }

    private void setOrientation(long i, JSONObject orData) {
        float th = ((Number)orData.get("_field0")).floatValue();
        float ph = ((Number)orData.get("_field1")).floatValue();
        world.actors.get(i).setOrientation(th,ph);
    }

    private void addObject(long id, JSONObject obj) {
        JSONObject posData = (JSONObject)(obj.get("pos"));
        JSONObject orData = (JSONObject)(obj.get("ori"));
        String type = (String)(obj.get("obj_type"));

        DrawObject newObj = new ModeledObject(type, getModel(type));
        world.actors.put(id, newObj);
        setPosition(id, posData);
        setOrientation(id, orData);
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
                if(cmdType.equals("InitializeWorld")) {
                    JSONObject world = (JSONObject)fields.get(0);
                    for(Object o : world.entrySet()) {
                        Map.Entry e = (Map.Entry)o;
                        //System.out.printf("(%s, %s)\n", e.getKey(), e.getValue());
                        long i = Long.parseLong((String)e.getKey(), 10);
                        addObject(i, (JSONObject)e.getValue());
                    }
                }
                else {
                    long i = (Long)fields.get(0);
                    if(cmdType.equals("SetPosition")) {
                        JSONObject posData = (JSONObject)fields.get(1);
                        setPosition(i, posData);
                    } else if(cmdType.equals("SetOrientation")) {
                        JSONObject orData = (JSONObject)fields.get(1);
                        setOrientation(i, orData);
                    } else if(cmdType.equals("AddObject")) {
                        addObject(i, (JSONObject)fields.get(1));
                    } else if(cmdType.equals("RemoveObject")){
                        world.actors.remove(i);
                    } else if(cmdType.equals("SetPlayerNumber")) {
                        world.playernum = i;
                    }
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
