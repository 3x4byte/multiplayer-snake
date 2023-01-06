import com.google.gson.annotations.Expose;

import java.util.Objects;

/**
 * Class representing Coordinates on a Snake Game Board, made serializable via GSON.
 */
class Coordinate {
    @Expose(serialize = false, deserialize = false)
    private transient final int hashCode;
    @Expose
    public int x;
    @Expose
    public int y;

    Coordinate(int x, int yPos) {
        this.x = x;
        this.y = yPos;
        hashCode = Objects.hash(x, yPos);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Coordinate) {
            Coordinate nObj = (Coordinate) obj;
            return this.x == nObj.x && this.y == nObj.y;
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
