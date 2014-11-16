package client;
import java.util.*;

public class GameObject {
    public Hashtable<Long,DrawObject> actors;

    public GameObject() {
        actors = new Hashtable<Long,DrawObject>();
    }
}
