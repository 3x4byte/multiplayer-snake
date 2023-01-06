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
    public transient OpCode direction = OpCode.UP; //the message OpCodes double as directional data - saves processing
    @Expose
    public boolean collided = false;
    @Expose
    private LinkedList<Coordinate> snakeFields;
    @Expose(serialize = false, deserialize = false)
    private transient HashSet<Coordinate> occupiedFields;


    Snake(){
        occupiedFields = new HashSet<>();
        snakeFields = new LinkedList<>();
        preoccupyFields();
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

    public void move(){
        Coordinate head = snakeFields.getFirst();

        switch (direction){
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
    }

    private void moveHelper(Coordinate targetField){
        snakeFields.addFirst(targetField);
        occupiedFields.remove(snakeFields.removeLast()); // delete the last snake body part
        collided = occupiedFields.contains(targetField) || targetField.x > Game.WORLD_WIDTH || targetField.x < Game.WORLD_WIDTH ||
                targetField.y > Game.WORLD_HEIGHT || targetField.y < Game.WORLD_HEIGHT;
    }



    /*
    public int[][] stringifySnake(){
        int[][] s = new int[snakeOrder.size()+1][];
        s[0] = new int[]{snakeId};
        int i = 1;
        for (Coordinate c: snakeOrder){
            s[i++] = new int[]{c.xPos, c.yPos};
        }
        return s;
    }

     */

    // todo eigentlich sollte das ein clientseitiger check sein!
    public void changeDirection(OpCode direction){
        switch (direction){
            case UP:
                if (!this.direction.equals(OpCode.DOWN)){
                    this.direction = direction;
                }
                break;
            case DOWN:
                if (!this.direction.equals(OpCode.UP)){
                    this.direction = direction;
                }
                break;
            case LEFT:
                if (!this.direction.equals(OpCode.RIGHT)){
                    this.direction = direction;
                }
                break;
            case RIGHT:
                if (!this.direction.equals(OpCode.LEFT)){
                    this.direction = direction;
                }
                break;
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
