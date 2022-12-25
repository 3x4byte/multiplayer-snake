import org.java_websocket.WebSocket;

/**
 * Holds runtime player data, is currently not persisted
 */
public class Player {
    public static int playerCount = 0;
    public final WebSocket connection; //technically redundant
    public final int id;

    public String subscribedToLobbyId;
    public Game.GameData gameData; // is reset at every game start

    Player(WebSocket connection){
        this.id = playerCount++;
        this.connection = connection;
    }

}
