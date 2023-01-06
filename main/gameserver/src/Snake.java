import Items.Apple;
import Items.Item;
import com.google.gson.annotations.Expose;

import java.util.*;

public class Snake {

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
    private LinkedList<Coordinate> snakeFields;
    @Expose(serialize = false, deserialize = false)
    public transient HashSet<Coordinate> occupiedFields;
    @Expose(serialize = false, deserialize = false)
    private final transient HashMap<Coordinate, Item> itemPositions;
    @Expose(serialize = false, deserialize = false)
    private final transient Set<Coordinate> collectedItems; //DO NOT represent player owned items - are used to delete items from itemPositions after every iteration
    Snake(HashMap<Coordinate, Item> itemPositions, Set<Coordinate> collectedItems){
        this.itemPositions = itemPositions;
        this.collectedItems = collectedItems;
        snakeToStartPosition();
        snakeMovementDataReset();
    }

    private void preoccupyFields(){
        for (Coordinate c : Arrays.asList(
                                    new Coordinate(5, 5),
                                    new Coordinate(5, 6),
                                    new Coordinate(5, 7))){
            snakeFields.addLast(c);
            occupiedFields.add(c);
        }
    }

    public void snakeToStartPosition(){
        this.occupiedFields = new HashSet<>();
        this.snakeFields = new LinkedList<>();
        preoccupyFields();
    }

    public void move(){
        Coordinate head = snakeFields.getFirst();

        synchronized (directionMutex) {
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

    private void moveHelper(Coordinate targetField){
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

            if (i == null){
                // there was no apple, last body part deleted
                occupiedFields.remove(snakeFields.removeLast());
            } else {
                // no matter the item, we remove it after every snake progressed (this equally fast snakes both get it - fairness)
                collectedItems.add(targetField);
                if (!(i instanceof Apple)) {
                    // delete the last snake body part if we did not eat an apple
                    occupiedFields.remove(snakeFields.removeLast());
                    //todo here we check for all the other items if we add any
                }
            }

        }
    }

    private OpCode getNextFromDirectionOrLast(){
        OpCode direction = nextDirections.pollFirst();
        if (direction == null){
            direction = lastDirection;
        }
        return direction;
    }

    private OpCode getLastFromDirectionOrLast(){
        OpCode direction = nextDirections.getLast();
        if (direction == null){
            direction = lastDirection;
        }
        return direction;
    }

    public void snakeMovementDataReset(){
        this.lastDirection = OpCode.UP;
        this.nextDirections = new BoundedQueue<>(3);
        this.collided = false;
    }

    public void changeDirection(OpCode direction){
        synchronized (directionMutex) {
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
