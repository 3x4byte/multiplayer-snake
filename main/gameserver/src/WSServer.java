import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.WebSocketListener;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Manages WSConnections for our {@link GameServer}
 * @param <T> the type of message used
 */
public class WSServer<T extends WSServer.Message> extends WebSocketServer{
    private static final Gson gsonBuilder = new GsonBuilder().create();
    private final Class<T> messageClass;

    private final MessageHandler<T> handler;
    private ConnectionEventListener<?> connectionEventListener;

    public WSServer(InetSocketAddress address, MessageHandler<T> handler, Class<T> messageClass){
        super(address);
        this.handler = handler;
        this.messageClass = messageClass;
    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (connectionEventListener != null){
            connectionEventListener.apply(ConnectionEvent.OPENED, conn, handshake, null);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (connectionEventListener != null){
            connectionEventListener.apply(ConnectionEvent.CLOSED, conn, null, reason);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Handling message " + message);
        T m = gsonBuilder.fromJson(message, messageClass);
        System.out.println("parsed Object: " + m.jsonify());
        m.setSender(conn);

        Optional<T> response = handler.handle(m);
        if (response.isPresent() && conn.isOpen()){
            conn.send(response.get().jsonify());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
    }

    @Override
    public void onStart() {

    }

    public void setOnConnectionEventListener(ConnectionEventListener<?> listener){
        this.connectionEventListener = listener;
    }


    interface Message{
        OpCode<?, ?> getOpcode();

        WebSocket getSender();

        /** Should parse OpCode and message content to a String*/
        String jsonify();

        /** Should parse Message content to the expected Object type : R*/
        <R> R getContent(Class<R> contentClass);

        void setSender(WebSocket conn);
    }

    interface OpCode<T, R>{
        Optional<R> getOpCode(T input);
    }

    @FunctionalInterface
    interface MessageHandler<T extends Message>{
        Optional<T> handle(T message);
    }

    enum ConnectionEvent{
        OPENED,
        CLOSED;
    }


    @FunctionalInterface
    interface ConnectionEventListener<T>{
        T apply(ConnectionEvent event, WebSocket conn, @Nullable ClientHandshake handshake, @Nullable String reason);
    }


}