import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Eye-Candy mapping of String OpCodes to Enums.
 * Serves as our WS-API and is used to determine the way a  {@link WSServer.Message},
 * more specifically a  {@link WSMessage} has to be handled by {@link WSServer.MessageHandler}
 */
enum OpCode implements WSServer.OpCode<String, OpCode> {
    ZERO("UNIQUE"),
    JOIN_LOBBY("JOIN"),
    JOIN_FAILED("JOIN_FAILED"),
    LEAVE_LOBBY("LEAVE"),
    UP("W"),
    DOWN("S"),
    LEFT("A"),
    RIGHT("D"),
    PLAYER_POSITIONS("POSITIONS") //"POSITIONS-[[["ID"],["43","66"],["44","66"],["45","66"]]]"
    ;

    final String id;

    OpCode(String id){
        this.id = id;
    }
    private static final Map<String, OpCode> lookup = new HashMap<>();

    static {
        for (OpCode code : OpCode.values()) {
            lookup.put(code.id, code);
        }
    }

    @Override
    public Optional<OpCode> getOpCode(String input) {
        return Optional.ofNullable(lookup.get(input));
    }
}