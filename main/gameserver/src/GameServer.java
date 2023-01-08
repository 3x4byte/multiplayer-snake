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
    private boolean isRunning;
    private final Random random = new Random();
    private final Object lobbyCreationMutex = new Object();

    private final WSServer<WSMessage> server;
    private final ConcurrentMap<WebSocket, Player> players = new ConcurrentHashMap<>(); //requires concurrent - thread safe access
    private final ConcurrentMap<String, Lobby> lobbies = new ConcurrentHashMap<>(); //maps lobby ids to lobbies

    ExecutorService executorService = Executors.newFixedThreadPool(4);
    private GameServer(){
        server = new WSServer<WSMessage>(new InetSocketAddress(PORT), new WSMessageHandler(), WSMessage.class);
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
                case CONFIGURE_LOBBY:
                    return handleConfigureLobby(message);
                case CREATE_LOBBY:
                    return handleCreateLobby(message);
                case UP: //intentional fall throughs
                case DOWN:
                case LEFT:
                case RIGHT:
                    return handlePlayerMove(message);
                default:
                    System.out.println("COULD NOT IDENTIFY DATA: " + message);
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
                            Player p = new Player(conn);
                            players.put(conn, p);
                            System.out.println("player: " + players.get(conn).id + " arrived");

                            // todo only for lobby output testing
                            Lobby l = new Lobby("A");
                            System.out.println("Player joined" + l.join(p));
                            l.startGame();
                            lobbies.put("A", l);
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
        this.isRunning = true;
        while (isRunning){
            for (Map.Entry<String, Lobby> lobbyEntry : lobbies.entrySet()) {
                Lobby lobby = lobbyEntry.getValue();
                long currentTime = System.currentTimeMillis();
                synchronized (lobby.game.lastUpdatedAtRWMutex) {
                    if (lobby.game.state.equals(Game.State.RUNNING) && (currentTime - lobby.game.lastUpdatedAt) >= lobby.game.fastestSnakeSpeed) {
                        lobby.game.lastUpdatedAt = currentTime;
                        System.out.println("UPDATING LOBBY");
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
        gameServer.run();
    }


    // WS HANDLERS

    /**
     * @return Optional Message indicating that the lobby is full
     */
    public Optional<WSMessage> handleJoinLobby(WSMessage message){
        Player player = players.get(message.getSender());
        String[] msg = message.getContent(String[].class);
        if (! lobbies.get(msg[1]).join(player)){
            return Optional.of(new WSMessage(OpCode.JOIN_FAILED));
        }
        return Optional.empty();
    }

    /**
     * @return an error if the lobby could not be left, else nothing
     */
    public Optional<WSMessage> handleLeaveLobby(WSMessage message){
        Player player = players.get(message.getSender());
        boolean leftLobbySuccess = lobbies.get(player.subscribedToLobbyId).leave(player);
        player.subscribedToLobbyId = null;
        if (! leftLobbySuccess){
            return Optional.of(new WSMessage(OpCode.JOIN_FAILED));
        }
        return Optional.empty();
    }


    /**
     * @return always empty optional
     */
    public Optional<WSMessage> handlePlayerMove(WSMessage message){
        Player player = players.get(message.getSender());
        player.snake.changeDirection(message.getOpcode()); //gameData should not be null
        return Optional.empty();
    }

    public Optional<WSMessage> handleConfigureLobby(WSMessage message){
        String lobbyCode;
        Lobby lobby;
        synchronized (lobbyCreationMutex) {
            do {
                lobbyCode = "#" + random.nextInt(1000);
            } while (lobbies.containsKey(lobbyCode));
            lobby = new Lobby(lobbyCode);
            lobbies.put(lobbyCode, lobby);
        }

        WSMessage response = new WSMessage(OpCode.CONFIGURE_LOBBY_RESPONSE, lobby);
        return Optional.of(response);
    }

    public Optional<WSMessage> handleCreateLobby(WSMessage message){
        Lobby lobby = message.getContent(Lobby.class);
        lobby.join(players.get(message.getSender()));
        lobbies.put(lobby.ID, lobby); //update the lobby

        WSMessage response = new WSMessage(OpCode.CREATE_LOBBY_RESPONSE, lobby);
        return Optional.of(response);
    }

}
