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

    public int apples; // the amount of apples that should be present at all time

    public State state = State.CREATED;
    public Map<String, Player> participants; // maps player IDs to Player Objects

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
    public float fastestSnakeSpeed = SNAKE_SPEED; // todo, if we ever have speed increasing items, multipliers may be applied here. The update speed of a lobby always depends on the fastest snake
    private long roundLengthMS;
    private long timeTillNextDeathMS;
    private long lastUpdatedAt;
    private final Random random = new Random();

    /**
     * Will be called by a worker Thread of the GameServer to progress the game
     */
    public Runnable RunGame = new Runnable() {
        @Override
        public void run() {
            // resets game state
            state = State.RUNNING;
            roundLengthMS = 20000;
            timeTillNextDeathMS = roundLengthMS;
            collectedItems.clear();
            itemCoordinates.clear();
            apples = (int) Math.ceil(participants.size() / 2.0f) + 1;

            // sends next death information to player
            String timeTillNextDeathMessage = new WSMessage(OpCode.NEXT_PLAYER_DEATH, roundLengthMS).jsonify();
            for (Player p : participants.values()){
                if (p.connection.isOpen()){
                    p.connection.send(timeTillNextDeathMessage);
                }
            }

            gameloop();
        }
    };

    enum State {
        CREATED,
        RUNNING,
        STOPPED;
    }

    public void setMembers(Map<String, Player> participants) {
        this.participants = participants;
        for (Player player :participants.values()){
            player.snake = new Snake(itemCoordinates, collectedItems);
        }
    }

    /**
     * Progress the lobby state by one tick
     * Holds the entire Games logic. And determines the GameState for this tick.
     * Once necessary information has been computed the users are updated accordingly.
     *
     * all player state updates are handles as essentially round trip data - the players game is not extrapolated but updates only on websocket msg.
     */
    private void progress(long now){
        Player[] players = new Player[participants.size()];
        int i = 0;
        int deadPlayers = 0;
        int shortestLength = Integer.MAX_VALUE;

        Set<Map.Entry<String, Player>> entries = participants.entrySet();
        for (Map.Entry<String, Player> entrySet : entries){
            Player player = entrySet.getValue();
            if (player.snake.isAlive()) {

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
            // reset
            lastUpdatedAt = now;
            timeTillNextDeathMS = roundLengthMS;

            // prepare msg
            WSMessage message = new WSMessage(OpCode.NEXT_PLAYER_DEATH, timeTillNextDeathMS);
            String nextPlayerDeathMessge = message.jsonify();

            // subtract length and send next "kill" duration
            for (Map.Entry<String, Player> entrySet : entries) {
                Player player = entrySet.getValue();
                if (player.snake.isAlive()) {
                    player.snake.trimOrDie(shortestLength);
                    if (player.connection.isOpen()) {
                        player.connection.send(nextPlayerDeathMessge);
                    }
                }
            }
        }

        // update the scores and end the game if its over
        if(deadPlayers == participants.size()) { //todo add -1  if we want to notify once one man standing
            this.state = State.STOPPED;
            sendScores();
        }

        removeCollectedItems();
        spawnApples(); //todo spawn different items later

        // prepare messages
        WSMessage messageApplePositions = new WSMessage(OpCode.ITEM_POSITIONS, itemCoordinates);
        String applePositionsAsJson = messageApplePositions.jsonify();
        WSMessage messagePlayerPositions = new WSMessage(OpCode.PLAYER_POSITIONS, players);
        String playerPositionsAsJson = messagePlayerPositions.jsonify();
        // send the "snakes" data to all players
        for (Map.Entry<String, Player> entrySet : entries){
            Player player = entrySet.getValue();

            if (player.connection.isOpen()) {
                player.connection.send(applePositionsAsJson);
                player.connection.send(playerPositionsAsJson); // applePositions have to be sent first (UI)
            }
        }
    }


    public void gameloop(){
        while (state.equals(State.RUNNING)){
            lastUpdatedAt = System.currentTimeMillis();

            try {
                Thread.sleep((long) TICK_DURATION);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            progress(System.currentTimeMillis());
        }
    }


    /**
     * spawns apples on free fields so there are at all times
     * {@link #apples} times apples on the field
     * this method may also be used to spawn different items
     */
    private void spawnApples(){
        HashSet<Coordinate> fieldCopy = new HashSet<>(fields);
        for (Map.Entry<String, Player> entrySet : participants.entrySet()){
            Player player = entrySet.getValue();
            fieldCopy.removeAll(player.snake.occupiedFields);
        }
        fieldCopy.removeAll(itemCoordinates.keySet());

        Map<Coordinate, Item> newApplePositions = new HashMap<>(apples - itemCoordinates.size());
        Coordinate[] fieldCopyArray = fieldCopy.toArray(new Coordinate[fieldCopy.size()]);
        for (int x = 0; x < apples- itemCoordinates.size(); x++){
            newApplePositions.put(fieldCopyArray[random.nextInt(fieldCopyArray.length)], Item.Apple);
        }

        itemCoordinates.putAll(newApplePositions);
    }

    /**
     * Each player tracks the items he collected, after a player update cycle these items are removed from the pool
     */
    private void removeCollectedItems(){
        collectedItems.forEach(itemCoordinates::remove);
        participants.values().forEach(p -> p.snake.collectedItems.clear());
    }

    /**
     * Sends the scores to the users as a list of Player Objects sorted by the time
     * they "died" in descending order -> the last one to die is the first player in the list
     */
    private void sendScores(){
        List<Player> players = new ArrayList<>(participants.values());
        players.sort(new ScoreComparator().reversed());
        WSMessage stats = new WSMessage(OpCode.GAME_STOPPED, players);
        String scoreMessage = stats.jsonify();
        for (Player p : players){
            if (p.connection.isOpen()){
                p.connection.send(scoreMessage);
            }
        }
    }

    /**
     * Sorts players by the time they died in ascending Order
     */
    public static class ScoreComparator implements Comparator<Player> {

        @Override
        public int compare(Player o1, Player o2) {
            return Long.compare(o1.snake.diedAt, o2.snake.diedAt);
        }
    }

}
