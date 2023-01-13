import java.util.*;


/**
 * The class that will progress a Game on a per-Lobby basis
 * (e.g. handles spawning of apples, collisions, everything)
 */
public class Game {
    public static final float CYCLE_DURATION_MS = 1000f;
    public static final float TICKS_PER_CYCLE = 3f;
    public static final float TICK_DURATION = CYCLE_DURATION_MS / TICKS_PER_CYCLE;
    public static final float SNAKE_SPEED = TICK_DURATION; // the snake speed equals the TICK_DURATION, which as of right now is 1/s

    public static final int WORLD_WIDTH = 10;
    public static final int WORLD_HEIGHT = WORLD_WIDTH;

    public static final int apples = 2; // the amount of apples that should be present at all time

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

    private final Map<Coordinate, Item> itemCoordinates = new HashMap<>(apples);
    private final Set<Coordinate> collectedItems = new HashSet<>();

    // GAME DATA
    // The Snakes speed determines how long it takes per field
    public float fastestSnakeSpeed = SNAKE_SPEED; // multipliers may be applied here. The update speed of a lobby always depends on the fastest snake
    private long roundLengthMS = 20000;
    private long timeTillNextDeathMS = roundLengthMS;
    public long lastUpdatedAt = 0;
    private Random random = new Random();

    /**
     * Will be called by a worker Thread of the GameServer to progress the game
     */
    public Runnable RunGame = new Runnable() {
        @Override
        public void run() {
            gameloop();
        }
    };


    enum State {
        RUNNING,
        STOPPED;
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
    private void progress(long now){
        Player[] players = new Player[participants.size()];
        int i = 0;
        int deadPlayers = 0;
        int shortestLength = Integer.MAX_VALUE;

        Set<Map.Entry<Integer, Player>> entries = participants.entrySet();
        for (Map.Entry<Integer, Player> entrySet : entries){
            Player player = entrySet.getValue();
            if (player.snake.lives > 0) {

                if (player.snake.collided) {
                    player.snake.snakeToStartPosition();
                    player.snake.snakeMovementDataReset();
                } else {
                    player.snake.move();
                    int length = player.snake.occupiedFields.size();
                    if (length < shortestLength){
                        shortestLength = length;
                    }
                }

                players[i++] = player;
            } else {
                deadPlayers += 1;
            }
        }

        timeTillNextDeathMS -= now-lastUpdatedAt;
        if (timeTillNextDeathMS <= 0) {
            for (Map.Entry<Integer, Player> entrySet : entries) {
                Player player = entrySet.getValue();
                player.snake.trimOrDie(shortestLength);
            }
            lastUpdatedAt = now;
            timeTillNextDeathMS = roundLengthMS;
        }

        if(deadPlayers == participants.size()) {
            this.state = State.STOPPED;
        }

        removeCollectedItems();
        spawnApples(); //todo spawn different items later

        // prepare messages
        WSMessage messageApplePositions = new WSMessage(OpCode.ITEM_POSITIONS, itemCoordinates);
        String applePositionsAsJson = messageApplePositions.jsonify();
        WSMessage messagePlayerPositions = new WSMessage(OpCode.PLAYER_POSITIONS, players);
        String playerPositionsAsJson = messagePlayerPositions.jsonify();
        // send the "snakes" data to all players
        for (Map.Entry<Integer, Player> entrySet : entries){
            Player player = entrySet.getValue();

            if (player.connection.isOpen()) {
                //System.out.println("apples: " + applePositionsAsJson);
                player.connection.send(applePositionsAsJson);
                player.connection.send(playerPositionsAsJson); // applePositions have to be sent first (UI)
            }
        }
    }

    public void gameloop(){
        lastUpdatedAt = System.currentTimeMillis();

        while (state.equals(State.RUNNING)){

            if (random.nextInt(100) < 15) {
                throw new UnsupportedOperationException();
            }

            try {
                Thread.sleep((long) TICK_DURATION);
                //System.out.println("updaing lobby");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            progress(System.currentTimeMillis());
            gameloop();
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
            newApplePositions.put(fieldCopyArray[random.nextInt(fieldCopyArray.length)], Item.Apple);
        }

        //System.out.println("ADDING new apples: " + newApplePositions);
        itemCoordinates.putAll(newApplePositions);
    }

    private void removeCollectedItems(){
        collectedItems.forEach(itemCoordinates::remove);
        participants.values().forEach(p -> p.snake.collectedItems.clear());
    }

}
