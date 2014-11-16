package client;
import java.util.*;

public class GameObject {
    public HashTable<Integer,DrawObject> actors;

    public GameObject() {
        actors = new ArrayList<DrawObject>();
    }
}
