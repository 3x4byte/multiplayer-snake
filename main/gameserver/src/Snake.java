import java.util.*;
import java.util.logging.Level;

public class Snake {

    public final int snakeId;
    public int length = 3;
    public float speedFactor = 1;
    public int lives = 3; //?
    public OpCode direction = OpCode.UP; //the message OpCodes double as directional data - saves processing
    public boolean collided = false;

    private LinkedList<Coordinate> snakeOrder;
    private HashSet<Coordinate> occupiedFields;


    Snake(int id){
        this.snakeId = id;
        occupiedFields = new HashSet<>();
        snakeOrder = new LinkedList<>();
        preoccupyFields();
    }

    private void preoccupyFields(){
        for (Coordinate c : Arrays.asList(
                                    new Coordinate(5, 5),
                                    new Coordinate(5, 6),
                                    new Coordinate(5, 7))){
            snakeOrder.addLast(c);
            occupiedFields.add(c);
        }
    }

    public void move(){
        Coordinate head = snakeOrder.getFirst();

        switch (direction){
            case UP:
                moveHelper(new Coordinate(head.xPos, head.yPos - 1)); //todo collision stuff
                break;
            case DOWN:
                moveHelper(new Coordinate(head.xPos, head.yPos + 1));
                break;
            case RIGHT:
                moveHelper(new Coordinate(head.xPos + 1, head.yPos));
                break;
            default:
            case LEFT:
                moveHelper(new Coordinate(head.xPos - 1, head.yPos));
                break;
        }
    }

    private void moveHelper(Coordinate targetField){
        snakeOrder.addFirst(targetField);
        occupiedFields.remove(snakeOrder.removeLast()); // delete the last snake body part
        collided = occupiedFields.contains(targetField) || targetField.xPos > Game.WORLD_WIDTH || targetField.xPos < Game.WORLD_WIDTH ||
                targetField.yPos > Game.WORLD_HEIGHT || targetField.yPos < Game.WORLD_HEIGHT;
    }



    /**
     * Parse the snake as String array of coordinates [[x1, y1], [x2, y2], ...] in order Head - Body - Tail
     */
    public int[][] stringifySnake(){
        int[][] s = new int[snakeOrder.size()+1][];
        s[0] = new int[]{snakeId};
        int i = 1;
        for (Coordinate c: snakeOrder){
            s[i++] = new int[]{c.xPos, c.yPos};
        }
        return s;
    }

    // todo eigentlich sollte das ein clientseitiger check sein!
    public void changeDirection(OpCode direction){
        switch (direction){
            case UP:
                if (!this.direction.equals(OpCode.DOWN)){
                    this.direction = direction;
                }
            case DOWN:
                if (!this.direction.equals(OpCode.DOWN)){
                    this.direction = direction;
                }
            case LEFT:
                if (!this.direction.equals(OpCode.DOWN)){
                    this.direction = direction;
                }
            case RIGHT:
                if (!this.direction.equals(OpCode.DOWN)){
                    this.direction = direction;
                }
        }
    }


    static class Coordinate{
        public final int hashCode;
        public int xPos;
        public int yPos;

        Coordinate(int xPos, int yPos){
            this.xPos = xPos;
            this.yPos = yPos;
            hashCode = Objects.hash(xPos, yPos);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof  Coordinate){
                Coordinate nObj = (Coordinate) obj;
                return this.xPos ==nObj.yPos && this.yPos == nObj.yPos;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return "( " + xPos + ", " + yPos + ")";
        }
    }
}
