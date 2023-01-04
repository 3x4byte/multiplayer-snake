import java.util.Arrays;
import java.util.Map;
import org.json.JSONArray;


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

    public long lastUpdatedAt = 0; //lobby will be updated as soon as it is created
    public final Object lastUpdatedAtRWMutex = new Object();
    public State state = State.RUNNING;
    public final Map<Integer, Player> participants; // maps player IDs to Player Objects - in the future will allow to target actions from players to players

    // GAME DATA
    // The Snakes speed determines how long it takes per field
    public float fastestSnakeSpeed = SNAKE_SPEED; // multipliers may be applied here. The update speed of a lobby always depends on the fastest snake
    private long timeTillNextDeath;
    private Player currentlyLongest;


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
            player.snake = new Snake(player.id);
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
        String[][][] positionalDataForUsers = new String[participants.size()][][];
        int i = 0;
        for (Map.Entry<Integer, Player> entrySet : participants.entrySet()){
            Player player = entrySet.getValue();
            System.out.println("UPDATING PLAYER " + player.id);
            if (player.snake.lives > 0) {
                /*
                if (player.snake.collided) {
                    //todo spawn snake in middle and subtract one live
                } else {
                    player.snake.move();
                }

                 */
                player.snake.move();
                positionalDataForUsers[i++] = player.snake.stringifySnake();
            }
        }

        // send the positional data to all players
        for (Map.Entry<Integer, Player> entrySet : participants.entrySet()){
            Player player = entrySet.getValue();
            JSONArray jsonArray = new JSONArray(positionalDataForUsers);
            String[] msg = new String[]{OpCode.PLAYER_POSITIONS.id, jsonArray.toString()};
            System.out.println("OUTGOING DATA TO USER: " + Arrays.toString(msg));
            WSMessage message = new WSMessage(msg);
            player.connection.send(message.parseToString());
        }
    }


}
