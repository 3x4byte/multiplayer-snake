import com.google.gson.annotations.Expose;
import org.java_websocket.WebSocket;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds runtime player data, is currently not persisted
 */
public class Player {
    @Expose(serialize = false, deserialize = false)
    public transient static AtomicInteger playerCount = new AtomicInteger(0);
    @Expose(serialize = false, deserialize = false)
    public transient final WebSocket connection; //technically redundant

    @Expose
    public final String id;
    @Expose
    public String name;

    @Expose(serialize = false, deserialize = false)
    public transient String subscribedToLobbyId;
    @Expose
    public Snake snake; // is reset at every game start

    private final transient int hashCode;


    Player(WebSocket connection){
        this.id = playerCount.get() + "";
        playerCount.set(playerCount.get() + 1);
        this.connection = connection;
        this.hashCode = Objects.hash(id, connection);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Player) {
            Player nOjb = (Player) obj;
            return nOjb.id.equals(this.id) && nOjb.connection.equals(this.connection);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}
