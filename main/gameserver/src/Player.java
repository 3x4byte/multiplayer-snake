import com.google.gson.annotations.Expose;
import org.java_websocket.WebSocket;

/**
 * Holds runtime player data, is currently not persisted
 */
public class Player {
    @Expose(serialize = false, deserialize = false)
    public transient static int playerCount = 0;
    @Expose(serialize = false, deserialize = false)
    public transient final WebSocket connection; //technically redundant

    @Expose
    public final int id;
    @Expose
    public String name;

    @Expose(serialize = false, deserialize = false)
    public transient String subscribedToLobbyId;
    @Expose
    public Snake snake; // is reset at every game start

    Player(WebSocket connection){
        this.id = playerCount++;
        this.connection = connection;
    }


}
