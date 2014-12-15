package client;
import java.util.*;

public class GameWorld {
    public Map<Long,DrawObject> actors;

    public GameWorld() {
        actors = new LinkedHashMap<Long,DrawObject>();
    }

    public DrawObject getPlayer() {
        return actors.get(-1);
    }
}
