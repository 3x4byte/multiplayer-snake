public class GameServer {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("TEST");
        Object o = new Object();
        synchronized (o) {
            o.wait();
        }}
}
