import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Manages WSConnections for our {@link GameServer}
 * @param <T> the type of message used
 */
public class WSServer<T extends WSServer.Message<T>> extends WebSocketServer{
    private final MessageHandler<T> handler;
    private final T messageClass;
    private ConnectionEventListener<?> connectionEventListener;

    public WSServer(InetSocketAddress address, MessageHandler<T> handler, T messageClass){
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
        Optional<T> response = handler.handle(messageClass.parseToMessage(conn, message));
        if (response.isPresent() && conn.isOpen()){
            conn.send(response.get().parseToString());
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


    interface Message<T>{
        public OpCode<?, ?> getOpcode();

        public WebSocket getSender();

        /** Should parse conn and String message to a Message Obj*/
        public T parseToMessage(WebSocket conn, String message);
        /** Should parse OpCode and message content to a String*/
        public String parseToString();
    }

    interface OpCode<T, R>{
        Optional<R> getOpCode(T input);
    }

    @FunctionalInterface
    interface MessageHandler<T extends Message<T>>{
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