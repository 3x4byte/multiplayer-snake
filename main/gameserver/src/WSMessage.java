import org.java_websocket.WebSocket;

public class WSMessage implements WSServer.Message<WSMessage>{

    public static final String INFORMATION_DELIMITER = "-";

    private WebSocket connection;
    private String[] contents;

    WSMessage(WebSocket connection, String[] contents){
        this.connection = connection;
        this.contents = contents;
    }

    WSMessage(){}

    @Override
    public OpCode getOpcode() {
        return OpCode.ZERO.getOpCode(contents[0]).orElse(OpCode.ZERO);
    }

    @Override
    public WebSocket getSender() {
        return connection;
    }

    @Override
    public String parseToString() {
        return null;
    }

    @Override
    public WSMessage parseToMessage(WebSocket conn, String message) {
        return new WSMessage(conn, message.split(INFORMATION_DELIMITER));
    }

    /**
    Returns the Messages content including the OpCode at Position 0
     */
    public String[] getMessage() {
        return contents;
    }
}
