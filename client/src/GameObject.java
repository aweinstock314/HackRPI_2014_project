package client;
import java.util.*;

public class GameObject {
    public Hashtable<Integer,DrawObject> actors;

    public GameObject() {
        actors = new Hashtable<Integer,DrawObject>();
    }
}
