import Items.Apple;
import Items.Item;

import java.util.*;


/**
 * The class that will progress a Game on a per-Lobby basis
 * (e.g. handles spawning of apples, collisions, everything)
 */
public class Game {
    public static final float CYCLE_DURATION_MS = 1000f;
    public static final float TICKS_PER_CYCLE = 1f;
    public static final float TICK_DURATION = CYCLE_DURATION_MS / TICKS_PER_CYCLE;
    public static final float SNAKE_SPEED = TICK_DURATION; // the snake speed equals the TICK_DURATION, which as of right now is 1/s

    public static final int WORLD_WIDTH = 10;
    public static final int WORLD_HEIGHT = WORLD_WIDTH;

    public static final int apples = 1; // the amount of apples that should be present at all time

    public long lastUpdatedAt = 0; //lobby will be updated as soon as it is created
    public final Object lastUpdatedAtRWMutex = new Object();
    public State state = State.RUNNING;
    public final Map<Integer, Player> participants; // maps player IDs to Player Objects - in the future will allow to target actions from players to players

    private static final HashSet<Coordinate> fields = new HashSet<>(100);
    static {
        for (int x = 0; x<WORLD_WIDTH; x++){
            for (int y = 0; y<WORLD_HEIGHT; y++){
                fields.add(new Coordinate(x, y));
            }
        }
    }

    private final HashMap<Coordinate, Item> itemCoordinates = new HashMap<>(apples);
    private final Set<Coordinate> collectedItems = new HashSet<>();

    // GAME DATA
    // The Snakes speed determines how long it takes per field
    public float fastestSnakeSpeed = SNAKE_SPEED; // multipliers may be applied here. The update speed of a lobby always depends on the fastest snake
    private long timeTillNextDeath;
    private Player currentlyLongest;

    private Random random = new Random();

    /**
     * Will be called by a worker Thread of the GameServer to progress the game
     */
    public Runnable update = new Runnable() {
        @Override
        public void run() {
            progress();
        }
    };

    enum State {
        RUNNING,
        STOPPED,
    }

    Game(Map<Integer, Player> participants){
        this.participants = participants;
        for (Player player :participants.values()){
            player.snake = new Snake(itemCoordinates, collectedItems);
        }
    }

    /**
     * Progress the lobby state by one tick
     * Holds the entire Games logic. After the current GameState is
     * determined updates all clients.
     *
     * all player state updates are handles as essentially round trip data - the players game is not extrapolated but updates only on websocket msg.
     */
    private void progress(){
        Player[] players = new Player[participants.size()];
        int i = 0;
        for (Map.Entry<Integer, Player> entrySet : participants.entrySet()){
            Player player = entrySet.getValue();
            System.out.println("UPDATING PLAYER " + player.id);
            if (player.snake.lives > 0) {

                if (player.snake.collided) {
                    player.snake.snakeToStartPosition();
                    player.snake.snakeMovementDataReset();
                } else {
                    player.snake.move();
                }

                players[i++] = player;
            }
        }

        removeCollectedItems();
        spawnApples(); //todo spawn different items later

        // prepare messages
        WSMessage messageApplePositions = new WSMessage(OpCode.ITEM_POSITIONS, itemCoordinates);
        String applePositionsAsJson = messageApplePositions.jsonify();
        WSMessage messagePlayerPositions = new WSMessage(OpCode.PLAYER_POSITIONS, players);
        String playerPositionsAsJson = messagePlayerPositions.jsonify();
        // send the "snakes" data to all players
        for (Map.Entry<Integer, Player> entrySet : participants.entrySet()){
            Player player = entrySet.getValue();

            if (player.connection.isOpen()) {
                player.connection.send(playerPositionsAsJson);
                System.out.println("apples: " + applePositionsAsJson);
                player.connection.send(applePositionsAsJson);
            }
        }
    }

    // puts apples at free fields but can be used in the future
    // to also add buffs / debuffs
    private void spawnApples(){
        HashSet<Coordinate> fieldCopy = new HashSet<>(fields);
        for (Map.Entry<Integer, Player> entrySet : participants.entrySet()){
            Player player = entrySet.getValue();
            fieldCopy.removeAll(player.snake.occupiedFields);
        }
        fieldCopy.removeAll(itemCoordinates.keySet());

        Map<Coordinate, Item> newApplePositions = new HashMap<>(apples- itemCoordinates.size());
        Coordinate[] fieldCopyArray = fieldCopy.toArray(new Coordinate[fieldCopy.size()]);
        for (int x = 0; x < apples- itemCoordinates.size(); x++){
            newApplePositions.put(fieldCopyArray[random.nextInt(fieldCopyArray.length)], new Apple());
        }

        System.out.println("ADDING new apples: " + newApplePositions);
        itemCoordinates.putAll(newApplePositions);

        System.out.println(new Coordinate(1, 2).equals(new Coordinate(1, 2)));
    }

    private void removeCollectedItems(){
        collectedItems.forEach(itemCoordinates::remove);
    }

}
