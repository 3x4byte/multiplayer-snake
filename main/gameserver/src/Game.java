import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * The class that will progress a Game on a per-Lobby basis
 * (e.g. handles spawning of apples, collisions, everything)
 */
public class Game {
    public static final float CYCLE_DURATION_MS = 1000f;
    public static final float TICKS_PER_CYCLE = 1f;
    public static final float TICK_DURATION = CYCLE_DURATION_MS / TICKS_PER_CYCLE;
    public static final float SNAKE_SPEED = TICK_DURATION; // the snake speed equals the TICK_DURATION, which as of richt now is 1/s

    public long lastUpdatedAt = Long.MIN_VALUE; //lobby will be updated as soon as it is created
    public final Object lastUpdatedAtRWMutex = new Object();
    public State state = State.STOPPED;
    public final Map<Integer, Player> participants; // maps player IDs to Player Objects - in the future will allow to target actions from players to players

    // GAME DATA
    // The Snakes speed determines how long it takes per field
    public float fastestSnakeSpeed = SNAKE_SPEED; // multipliers may be applied here. The update speed of a lobby always depends on the fastest snake
    private long timeTillNextDeath;
    private Player currentlyLongest;

    static class GameData{ //essentially the snake
        public int length;
        public float speedFactor = 1;
        public int lives; //?

    }

    /**
     * Will be called by a worker Thread of the GameServer to progress the game
     */
    public Runnable update = new Runnable() {
        @Override
        public void run() {
            synchronized (lastUpdatedAtRWMutex) {
                lastUpdatedAt = System.currentTimeMillis();
            }
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
            player.gameData = new GameData();
        }
    }

    /**
     * Progress the lobby state by one tick
     * Holds the entire Games logic. After the current GameState is
     * determined updates all clients.
     */
    private void progress(){
        //todo frage klären: round trip daten? -> bsp.: player -- turn l --> server -- update screen --> player
        // we do not handle websocket requests here (this is done outside), but we need to update all participants
    }


}
