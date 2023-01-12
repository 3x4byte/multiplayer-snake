import javax.annotation.Nullable;
import java.util.LinkedList;

/**
 * BoundedQueue is a size limited queue - currently only
 * #add and #addLast are implemented
 */
public class BoundedQueue<T> extends LinkedList<T> {

    private int limit;

    BoundedQueue(int limit){
        this.limit = limit;
    }

    @Override
    public boolean add(T t) {
        if (size() < this.limit) {
            return super.add(t);
        }
        return false;
    }

    @Override
    public void addLast(T t) {
        //System.out.println(" adding to queue" + t);
        if (size() < this.limit) {
            super.addLast(t);
        }
    }

    @Override
    @Nullable
    public T getLast() {
        if (size() > 0) {
            return super.getLast();
        }
        return null;
    }
}
