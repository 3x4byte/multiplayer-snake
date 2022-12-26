import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;

/**
 * Manages Multiplayer-Snake Games.
 * Defines the {@link WebSocket} behaviour via dependency injection.
 */
public class GameServer {
    private final int PORT = 5001;
    private boolean isRunning = true;
    private final WSServer<WSMessage> server;
    private final ConcurrentMap<WebSocket, Player> players = new ConcurrentHashMap<>(); //requires concurrent - thread safe access
    private final ConcurrentMap<String, Lobby> lobbies = new ConcurrentHashMap<>(); //maps lobby ids to lobbies

    ExecutorService executorService = Executors.newFixedThreadPool(4);
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
                //todo here we handle all the user sends per websocket
                // if the users move is tied to game data (lets say turn left).
                // we fetch him from the players map, get his game state, and apply changes.

                // example
                case JOIN_LOBBY:
                    return handleJoinLobby(message);
                case LEAVE_LOBBY:
                    return handleLeaveLobby(message);
                default:
                    return Optional.empty();
            }
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
                            players.put(conn, new Player(conn));
                            System.out.println("player: " + players.get(conn).id + " arrived");
                        break;
                    }
                    case CLOSED: {
                            System.out.println("player: " + players.get(conn).id + " left");
                            players.remove(conn);
                        break;
                    }
                }
            return null;
        }
    }

    /**
     * Continuously scans all the available lobbies to check if they need updates. This ensures
     * We do not have to use busy waiting and the Threads from the thread-pool executor are working always.
     * This further decouples the individual Games from a fixed Server Tick Time.
     */
    public void run(){

        while (isRunning){
            for (Map.Entry<String, Lobby> lobbyEntry : lobbies.entrySet()) {
                Lobby lobby = lobbyEntry.getValue();
                long currentTime = System.currentTimeMillis();
                synchronized (lobby.game.lastUpdatedAtRWMutex) {
                    if (lobby.game.state.equals(Game.State.RUNNING) && (currentTime - lobby.game.lastUpdatedAt) >= lobby.game.fastestSnakeSpeed) {
                        executorService.execute(lobby.game.update);
                    }
                }
            }
        }
    }

    /**
     * Eligible for spawning the GameServer
     */
    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        //gameServer.run();
    }


    // WS HANDLERS

    /**
     * @return Optional Message indicating that the lobby is full
     */
    public Optional<WSMessage> handleJoinLobby(WSMessage message){
        Player player = players.get(message.getSender());
        if (! lobbies.get(message.getMessage()[1]).join(player)){
            return Optional.of(new WSMessage(OpCode.JOIN_FAILED));
        }
        return Optional.empty();
    }

    public Optional<WSMessage> handleLeaveLobby(WSMessage message){
        Player player = players.get(message.getSender());
        //todo
        return Optional.empty();
    }

}
