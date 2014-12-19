package client;
import java.util.*;

public class GameWorld {
    public Map<Long,DrawObject> actors;
    public long playernum;

    public GameWorld() {
        actors = Collections.synchronizedMap(new LinkedHashMap<Long,DrawObject>());
    }

    public DrawObject getPlayer() {
        return actors.get(playernum);
    }
}
