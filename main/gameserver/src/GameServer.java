import com.google.gson.internal.LinkedTreeMap;
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
    private final Random random = new Random();
    private final Object lobbyCreationMutex = new Object();

    private final WSServer<WSMessage> server;
    private final ConcurrentMap<WebSocket, Player> players = new ConcurrentHashMap<>(); //requires concurrent - thread safe access
    private final ConcurrentMap<String, Lobby> lobbies = new ConcurrentHashMap<>(); //maps lobby ids to lobbies

    ExecutorService executorService;

    private GameServer(){
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);

        server = new WSServer<WSMessage>(new InetSocketAddress(PORT), new WSMessageHandler(), WSMessage.class);
        server.setOnConnectionEventListener(new OnConnectionEvent());
        server.start();

        executorService = Executors.newCachedThreadPool();
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
                case SET_NAME:
                    return handleSetName(message);
                case START_GAME:
                    return handleStartGame(message);
                case KICK_PLAYER:
                    handleKickPlayer(message);
                    break;
                //intentional fall throughs
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    return handlePlayerMove(message);
                default:
                    //System.out.println("COULD NOT IDENTIFY DATA: " + message);
                    return Optional.empty();
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
                            Player p = new Player(conn);
                            players.put(conn, p);
                            conn.send(new WSMessage(OpCode.CONNECTION_RESPONSE, p).jsonify());
                            //System.out.println("player: " + players.get(conn).id + " arrived");

                        break;
                    }
                    case CLOSED: {
                            //System.out.println("player: " + players.get(conn).id + " left");
                            handleLeaveLobby(new WSMessage(conn, OpCode.LEAVE_LOBBY, null));
                            players.remove(conn);
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
        GameServer gameServer = new GameServer();
    }


    // WS HANDLERS

    /**
     * @return Optional Message indicating that the lobby is full
     */
    public Optional<WSMessage> handleJoinLobby(WSMessage message){ //todo check if player is already subscribed to lobby!
        Player player = players.get(message.getSender());
        //System.out.println("LobbyId" + message.getContent(String.class));
        String lobbyId = message.getContent(String.class);
        Lobby lobby = lobbies.get(lobbyId);
        if (lobby.join(player)){
            sendLobbyUpdate(lobby);
            return Optional.of(new WSMessage(OpCode.JOIN_LOBBY_RESPONSE, lobby));
        }
        return Optional.empty();
    }

    /**
     * @return Message indicating whether leaving was successful or not
     */
    public Optional<WSMessage> handleLeaveLobby(WSMessage message){
        Player player = players.get(message.getSender());
        Lobby lobby =  lobbies.get(player.subscribedToLobbyId);
        boolean leftLobbySuccess = lobby.leave(player);

        // if the player left the lobby and the game is not running - notify users to update their lobby screen
        if (leftLobbySuccess && !lobby.game.state.equals(Game.State.RUNNING)) {
            WSMessage updateMessage = new WSMessage(OpCode.LOBBY_UPDATE, lobby);
            for (Player p: lobby.members.values()){
                if (p.connection.isOpen()){
                    p.connection.send(updateMessage.jsonify());
                }
            }
        }

        return Optional.of(new WSMessage(OpCode.LEAVE_LOBBY_RESPONSE, leftLobbySuccess));
    }


    /**
     * @return always empty optional
     */
    public Optional<WSMessage> handlePlayerMove(WSMessage message){
        System.out.println(message.getOpcode() + " sent ");
        Player player = players.get(message.getSender());
        player.snake.changeDirection(message.getOpcode()); //gameData should not be null
        return Optional.empty();
    }

    /**
     * Takes the lobby creation mutex and generates a random ID, once the Lobby
     * was generated using the ID, the lobby is sent to the player requesting the creation.
     */
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

        // workaround because GSON does not serialize the correct type for some reason
        LinkedTreeMap ltm = message.getContent(LinkedTreeMap.class);
        Lobby lobby = new Lobby((String) ltm.get("ID"));
        int lobbySize = Integer.parseInt((String) ltm.get("lobbySize"));
        lobbies.put(lobby.ID, lobby);
        lobby.owner = players.get(message.getSender());

        if (lobbySize < Lobby.MIN_LOBBY_SIZE) {
            lobby.lobbySize = Lobby.MIN_LOBBY_SIZE;
        } else if (lobbySize > Lobby.MAX_LOBBY_SIZE) {
            lobby.lobbySize = Lobby.MAX_LOBBY_SIZE;
        } else {
            lobby.lobbySize = lobbySize;
        }

        // auto joins the owner to the lobby
        handleJoinLobby(new WSMessage(message.getSender(), OpCode.ZERO, lobby.ID));

        // show lobby screen to owner
        WSMessage response = new WSMessage(OpCode.CREATE_LOBBY_RESPONSE, lobby);
        return Optional.of(response);
    }

    public Optional<WSMessage> handleSetName(WSMessage message){
        players.get(message.getSender()).name = message.getContent(String.class);
        return Optional.empty();
    }

    public Optional<WSMessage> handleStartGame(WSMessage message){
        Player caller = players.get(message.getSender());
        Lobby lobby = lobbies.get(caller.subscribedToLobbyId);

        if (lobby.owner.equals(caller)) {
            for (Player p : lobby.members.values()) {
                if (p.connection.isOpen()) {
                    //System.out.println("starting game for player + " + p.id);
                    p.connection.send(new WSMessage(OpCode.START_GAME_RESPONSE).jsonify());
                }
            }
            lobby.startGame();
            //executorService.submit(lobby.game.RunGame);
            new Thread(lobby.game.RunGame).start();

        }
        return Optional.empty();
    }

    /**
     * Sends the current lobby status to all players - used to update lobby screen!
     */
    private synchronized void sendLobbyUpdate(Lobby lobby){
        //System.out.println("in send lobby");
        WSMessage message = new WSMessage(OpCode.LOBBY_UPDATE, lobby);
        for (Player p : lobby.members.values()){
            if (p.connection.isOpen()){
                //System.out.println("sending ");
                p.connection.send(message.jsonify()); //todo entscheiden ob man alle daten an alle sendet oder immer nur neue (würde auch gehen)
            }
        }
    }

    private void handleKickPlayer(WSMessage message){
        System.out.println("");
        String playerId = message.getContent(String.class);
        Player owner = players.get(message.getSender());
        Lobby lobby = lobbies.get(owner.subscribedToLobbyId);
        Player kickedPlayer = lobby.members.get(playerId);

        if (lobby.owner.equals(owner) && !owner.equals(kickedPlayer)) {
            lobby.members.remove(playerId);
            sendLobbyUpdate(lobby);
        }

        if (kickedPlayer.connection.isOpen()) {
            kickedPlayer.connection.send(new WSMessage(OpCode.KICK_PLAYER_RESPONSE, null).jsonify());
        }
    }

}
