package client;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.*;

public class ModelCache {
    private static Map<String, JSONArray> models = new HashMap<String, JSONArray>();
    private static void populate(String type, String fname) throws IOException {
        models.put(type, (JSONArray)JSONValue.parse(new FileReader(fname)));
    }
    static {
        try {
            populate("Player", "player_model.json");
            populate("Sphere","unit_sphere.json");
            populate("Cylinder","unit_cylinder.json");
            populate("Triprism","unit_triprism.json");
        }
        catch(Exception e) { e.printStackTrace(); }
    }
    public static JSONArray get(String type) {
        JSONArray rv = models.get(type);
        if(rv == null) {
            throw new RuntimeException(String.format("Nonexistant model type \"%s\"", type));
        }
        else { return rv; }
    }
}
