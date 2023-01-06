import com.google.gson.annotations.Expose;

import java.util.*;

public class Snake {
    @Expose(serialize = false, deserialize = false)
    public transient int length = 3;
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
    public boolean collided = false;
    @Expose
    private LinkedList<Coordinate> snakeFields;
    @Expose(serialize = false, deserialize = false)
    private transient HashSet<Coordinate> occupiedFields;


    Snake(){
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
        snakeFields.addFirst(targetField);
        occupiedFields.remove(snakeFields.removeLast()); // delete the last snake body part
        collided = occupiedFields.contains(targetField) || targetField.x >= Game.WORLD_WIDTH || targetField.x < 0 ||
                targetField.y >= Game.WORLD_HEIGHT || targetField.y < 0;
        if (collided){
            lives -= 1;
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
    }

    // todo eigentlich sollte das ein clientseitiger check sein!
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


    static class Coordinate{
        @Expose(serialize = false, deserialize = false)
        private transient final int hashCode;
        @Expose
        public int x;
        @Expose
        public int y;

        Coordinate(int x, int yPos){
            this.x = x;
            this.y = yPos;
            hashCode = Objects.hash(x, yPos);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof  Coordinate){
                Coordinate nObj = (Coordinate) obj;
                return this.x ==nObj.y && this.y == nObj.y;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return "( " + x + ", " + y + ")";
        }
    }
}
