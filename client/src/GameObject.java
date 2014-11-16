package client;
import java.util.*;

public class GameObject {
    public Hashtable<Long,DrawObject> actors;

    public GameObject() {
        actors = new Hashtable<Long,DrawObject>();
    }

    public DrawObject getPlayer() {
        return actors.get(-1);
    }
}
