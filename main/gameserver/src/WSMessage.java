import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.java_websocket.WebSocket;

import java.util.Map;

/**
 * A Message Type used for abstraction of the data our WebSocket deals with.
 * We expect the following format for incoming messages: "Opcode-Content"
 * As JSON String
 * and accessible via: {@link WSMessage#getOpcode} and {@link WSMessage#getContent}
 */
public class WSMessage implements WSServer.Message{
    @Expose(serialize = false, deserialize = false)
    public static final Gson gsonBuilder = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .create();

    @Expose(serialize=false, deserialize=false)
    private WebSocket connection;
    @Expose
    private OpCode opCode;
    @Expose
    private Object content;


    /**
     * This Constructor will be used most of the time for sending
     */
    WSMessage(OpCode opCode,  Object contents){
        this.opCode = opCode;
        this.content = contents;
    }

    /**
     * Some Messages may just consist of an opcode
     * This one is for building responses for the {@link GameServer.WSMessageHandler} that only need minimal information
     */
    WSMessage(OpCode opCode){
        this.opCode = opCode;
    }


    WSMessage(){}

    @Override
    public OpCode getOpcode() {
        return this.opCode;
    }

    @Override
    public WebSocket getSender() {
        return connection;
    }

    @Override
    public String jsonify() {
        return gsonBuilder.toJson(this);
    }


    @Override
    public void setSender(WebSocket conn) {
        this.connection = conn;
    }

    @Override
    public <T> T getContent(Class<T> contentClass) {
        return contentClass.cast(this.content);
    }

    @Override
    public String toString() {
        return this.opCode + ": " + this.content;
    }
}
