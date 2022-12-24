import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


import java.util.Optional;

public class WSServer<T extends WSServer.Message<T>> extends WebSocketServer{
    private final MessageHandler<T> handler;
    private final T messageClass;
    private OnConnectionOpenedListener<?> listener;

    public WSServer(MessageHandler<T> handler, T messageClass){
        this.handler = handler;
        this.messageClass = messageClass;
    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (listener != null){
            listener.apply(conn, handshake);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        handler.handle(messageClass.parseToMessage(conn, message)).ifPresent(response -> conn.send(response.parseToString()));
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {

    }

    public void setOnConnectionOpenedListener(OnConnectionOpenedListener<?> listener){
        this.listener = listener;
    }

    interface Message<T>{
        public OpCode<?, ?> getOpcode();

        public WebSocket getSender();

        /** Should parse conn and String message to a Message Obj*/
        public T parseToMessage(WebSocket conn, String message);
        /** Should parse message content to a String*/
        public String parseToString();
    }

    interface OpCode<T, R>{
        Optional<R> getOpCode(T input);
    }

    @FunctionalInterface
    interface MessageHandler<T extends Message<T>>{
        Optional<T> handle(T message);
    }

    @FunctionalInterface
    interface OnConnectionOpenedListener<T>{
        T apply(WebSocket conn, ClientHandshake handshake);
    }
}