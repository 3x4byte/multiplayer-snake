import com.google.gson.annotations.Expose;

import java.util.*;

/**
 * Represents a classical Snake, made serializable via GSON.
 * It manages its movement and updates the game state by writing to injected Objects,
 * further provides methods for the Game instance to manage the snake Object.
 */
public class Snake {
    public static final int INITIAL_LENGTH = 3;

    @Expose(serialize = false, deserialize = false)
    public transient float speedFactor = 1;
    @Expose
    public int lives = 3; //?
    @Expose(serialize = false, deserialize = false)
    public transient OpCode lastDirection; //the message OpCodes double as directional data - saves processing
    @Expose(serialize = false, deserialize = false)
    private transient BoundedQueue<OpCode> nextDirections;
    @Expose(serialize = false, deserialize = false)
    private transient final Object directionMutex = new Object();
    @Expose
    public boolean collided;
    @Expose
    private LinkedList<Coordinate> snakeFields; //todo rename to body
    @Expose(serialize = false, deserialize = false)
    public transient HashSet<Coordinate> occupiedFields;
    @Expose(serialize = false, deserialize = false)
    private final transient Map<Coordinate, Item> itemPositions;
    @Expose(serialize = false, deserialize = false)
    public final transient Set<Coordinate> collectedItems; //DO NOT represent player owned items - are used to delete items from itemPositions after every iteration
    @Expose(serialize = false, deserialize = false)
    public transient boolean doesAcceptMovementData;


    Snake(Map<Coordinate, Item> itemPositions, Set<Coordinate> collectedItems){
        this.itemPositions = itemPositions;
        this.collectedItems = collectedItems;
        synchronized (directionMutex) {
            doesAcceptMovementData = true;
        }
        snakeToStartPosition();
        snakeMovementDataReset();
    }

    /**
     * Occupies fields for the snakes body
     */
    private void preoccupyFields(){
        for (Coordinate c : Arrays.asList(
                                    new Coordinate(5, 5),
                                    new Coordinate(5, 6),
                                    new Coordinate(5, 7))){
            snakeFields.addLast(c);
            occupiedFields.add(c);
        }
    }

    /**
     * Moves the snake to the starting position
     */
    public void snakeToStartPosition(){
        this.occupiedFields = new HashSet<>();
        this.snakeFields = new LinkedList<>();
        preoccupyFields();
    }

    /**
     * Moves the snake one step into the direction its heading.
     */
    public void move(){
        Coordinate head = snakeFields.getFirst();

        synchronized (directionMutex) {
            doesAcceptMovementData = true;
            OpCode nextDirection = getNextFromDirectionOrLast();

            switch (nextDirection) {
                case UP:
                    moveHelper(new Coordinate(head.x, head.y - 1)); //todo collision stuff
                    break;
                case DOWN:
                    moveHelper(new Coordinate(head.x, head.y + 1));
                    break;
                case RIGHT:
                    moveHelper(new Coordinate(head.x + 1, head.y));
                    break;
                default:
                case LEFT:
                    moveHelper(new Coordinate(head.x - 1, head.y));
                    break;
            }
            lastDirection = nextDirection;
        }
    }

    /**
     * Moves the snake onto the targetField and manages everything related to this action,
     * this includes: collisions, collection of items and the growth of the snake.
     */
    private void moveHelper(Coordinate targetField){
        // enables us to circle in with length 4 - this would not be possible if we first increased the size
        Coordinate removed = snakeFields.removeLast();
        occupiedFields.remove(removed);

        // move snake one field
        snakeFields.addFirst(targetField);
        collided = occupiedFields.contains(targetField) || targetField.x >= Game.WORLD_WIDTH || targetField.x < 0 ||
                targetField.y >= Game.WORLD_HEIGHT || targetField.y < 0;
        if (collided){
            lives -= 1;
        } else {
            occupiedFields.add(targetField);
            // check for items at that field
            Item i = itemPositions.get(targetField);

            if (i != null) {
                collectedItems.add(targetField);
                switch (i) {
                    case Apple:
                        snakeFields.addLast(removed);
                        occupiedFields.add(removed);
                        break;
                }
            }

            /* if we ever want to go back to the model where the snake can not circle with 4
            if (i == null){
                // there was no apple, last body part deleted
                occupiedFields.remove(snakeFields.removeLast());
            } else {
                // no matter the item, we remove it after every snake progressed (this equally fast snakes both get it - fairness)
                collectedItems.add(targetField);
                if (!(i.equals(Item.Apple))) {
                    // delete the last snake body part if we did not eat an apple
                    occupiedFields.remove(snakeFields.removeLast());
                }
            }
            */
        }
    }

    /**
     * Helper method either retrieving the next direction
     * form the queue (buffer) or returning the last used to update the snake.
     */
    private OpCode getNextFromDirectionOrLast(){
        OpCode direction = nextDirections.pollFirst();
        if (direction == null){
            direction = lastDirection;
        }
        return direction;
    }


    /**
     * Helper method either retrieving the last direction
     * form the queue (buffer) or returning the last used to update the snake.
     */
    private OpCode getLastFromDirectionOrLast(){
        OpCode direction = nextDirections.getLast();
        if (direction == null){
            direction = lastDirection;
        }
        return direction;
    }

    /**
     * Resets all fields related to the movement of the snake.
     */
    public void snakeMovementDataReset(){
        synchronized (directionMutex) {
            doesAcceptMovementData = false;
            this.lastDirection = OpCode.UP;
            this.nextDirections = new BoundedQueue<>(2);
            this.collided = false;
        }
    }

    /**
     * Method for the GameServer instance to set a direction change request made by the WebSocket.
     */
    public void changeDirection(OpCode direction){
        synchronized (directionMutex) {
            if (doesAcceptMovementData) {
                OpCode lastDirection = getLastFromDirectionOrLast();
                switch (direction) {
                    case UP:
                        if (!lastDirection.equals(OpCode.DOWN)) {
                            nextDirections.addLast(direction);
                        }
                        break;
                    case DOWN:
                        if (!lastDirection.equals(OpCode.UP)) {
                            nextDirections.addLast(direction);
                        }
                        break;
                    case LEFT:
                        if (!lastDirection.equals(OpCode.RIGHT)) {
                            nextDirections.addLast(direction);
                        }
                        break;
                    case RIGHT:
                        if (!lastDirection.equals(OpCode.LEFT)) {
                            nextDirections.addLast(direction);
                        }
                        break;
                }
            }
        }
    }

    public void trimOrDie(int size){
        int cutoffSize = size - INITIAL_LENGTH;
        if (size == occupiedFields.size()){
            lives -= 1;
        } else {
            for (int c = 0; c < cutoffSize; c++) {
                occupiedFields.remove(snakeFields.removeLast());
            }
        }
    }
}
