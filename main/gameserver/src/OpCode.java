/**
 * Eye-Candy mapping of String OpCodes to Enums.
 * Serves as our WS-API and is used to determine the way a  {@link WSServer.Message},
 * more specifically a  {@link WSMessage} has to be handled by {@link WSServer.MessageHandler}
 */
enum OpCode implements WSServer.OpCode {
    ZERO,
    CONNECTION_RESPONSE, // sends a player
    SET_NAME, //expects a string
    CONFIGURE_LOBBY, // nothing
    CONFIGURE_LOBBY_RESPONSE, // sends a lobby
    CREATE_LOBBY, // expects a lobby
    CREATE_LOBBY_RESPONSE, // sends a lobby
    START_GAME, // long
    START_GAME_RESPONSE, //nothing
    JOIN_LOBBY, // expects a string
    JOIN_LOBBY_RESPONSE, // sends a lobby
    JOIN_LOBBY_FAILED, // nothing
    LEAVE_LOBBY, // nothing
    LEAVE_LOBBY_RESPONSE, // sends a boolean
    LOBBY_UPDATE, // sends a lobby
    UP, // expects a string
    DOWN, // expects a string
    LEFT, // expects a string
    RIGHT, // expects a string
    PLAYER_POSITIONS, //sends a player objects list
    ITEM_POSITIONS, // sends a list of coordinates
    KICK_PLAYER, // expects a string
    KICK_PLAYER_RESPONSE, // nothing
    GAME_STOPPED // send list on players
    ;
}