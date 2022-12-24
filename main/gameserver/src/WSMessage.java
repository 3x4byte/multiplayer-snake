import org.java_websocket.WebSocket;

/**
 * A Message Type used for abstraction of the data our WebSocket deals with.
 * We expect the following StringFormat for incoming messages: OpCode-Data1-Data2-Data3 ...
 * which is then broken up internally into: [OpCode, Data1, Data2, Data3...]
 * and accessible via: {@link WSMessage#getOpcode} and {@link WSMessage#getMessage} whereas the latter
 * also contains the OpCode at the 0 index.
 */
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
