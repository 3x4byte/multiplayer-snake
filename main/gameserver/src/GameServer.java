import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * Manages Multiplayer-Snake Games.
 * Defines the {@link WebSocket} behaviour via dependency injection.
 */
public class GameServer {
    private final int PORT = 5001;
    private final WSServer<WSMessage> server;
    private final Map<WebSocket, Player> players = new HashMap<>(); //requires concurrent - thread safe access
    public final Object playerRWMutex = new Object();

    private GameServer(){
        server = new WSServer<>(new InetSocketAddress(PORT), new WSMessageHandler(), new WSMessage());
        server.setOnConnectionEventListener(new OnConnectionEvent());
        server.start();
    }

    /**
     * Handles incoming messages and is able to send responses
     * We expect the following StringFormat: OpCode-Data1-Data2-Data3 ...
     * whereas opcode is unique and allows to determine how to parse the message.
     * OpCodes are defined in {@link OpCode}
     */
    class WSMessageHandler implements WSServer.MessageHandler<WSMessage>{

        @Override
        public Optional<WSMessage> handle(WSMessage message) {
            switch (message.getOpcode()){
                //todo here is
            }
            return Optional.empty();
        }
    }

    /**
     * Manages what happens when a {@link WSServer.ConnectionEvent} fires
     */
    class OnConnectionEvent implements WSServer.ConnectionEventListener<Void>{

        @Override
        public Void apply(WSServer.ConnectionEvent event, WebSocket conn, @Nullable ClientHandshake handshake, @Nullable String reason) {
                switch (event){
                    case OPENED: {
                            synchronized (playerRWMutex) {
                                players.put(conn, new Player(conn));
                                System.out.println("player: " + players.get(conn).id + " arrived");
                            }
                        break;
                    }
                    case CLOSED: {
                            synchronized (playerRWMutex) {
                                System.out.println("player: " + players.get(conn).id + " left");
                                players.remove(conn);
                            }
                        break;
                    }
                }
            return null;
        }
    }

    /**
     * Eligible for spawning the GameServer
     */
    public static void main(String[] args) {
        GameServer g = new GameServer();
    }

}
