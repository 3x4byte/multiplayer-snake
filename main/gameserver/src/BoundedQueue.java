import javax.annotation.Nullable;
import java.util.LinkedList;

public class BoundedQueue<T> extends LinkedList<T> {

    private int limit;

    BoundedQueue(int limit){
        this.limit = limit;
    }

    @Override
    public boolean add(T t) {
        if (size() >= this.limit) {
            removeLast();
        }
        return super.add(t);
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
