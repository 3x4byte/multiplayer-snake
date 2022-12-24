import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

enum OpCode implements WSServer.OpCode<String, OpCode> {
    ZERO("0"),
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