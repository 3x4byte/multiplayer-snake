/**
 * Eye-Candy mapping of String OpCodes to Enums.
 * Serves as our WS-API and is used to determine the way a  {@link WSServer.Message},
 * more specifically a  {@link WSMessage} has to be handled by {@link WSServer.MessageHandler}
 */
enum OpCode implements WSServer.OpCode {
    ZERO,

    CONFIGURE_LOBBY,
    CONFIGURE_LOBBY_RESPONSE,
    CREATE_LOBBY,
    CREATE_LOBBY_RESPONSE,
    JOIN_LOBBY,
    JOIN_FAILED,
    LEAVE_LOBBY,
    UP,
    DOWN,
    LEFT,
    RIGHT,
    PLAYER_POSITIONS, //Player Objects List
    ITEM_POSITIONS // List of Coordinates
    ;
}