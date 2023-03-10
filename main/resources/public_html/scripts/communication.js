/**
 * Represents a Message sent via Websocket
 */
class Message {
    constructor(opCode, content) {
        this.opCode = opCode;
        this.content = content;
    }

    /**
     * parses a jsonString to a Message Object
     */
    static fromJson(jsonString) {
        const obj = JSON.parse(jsonString);
        return new Message(obj.opCode, obj.content);
    }

    /**
     * creates a jsonString from the Message Object called on
     */
    toJson() {
        return JSON.stringify({
            opCode: this.opCode,
            content: this.content
        });
    }
}

/**
 * Makes Messages sent via Websocket identifiable
 */
const OpCode = {
    ZERO: 'ZERO',
    CONNECTION_RESPONSE: 'CONNECTION_RESPONSE',
    SET_NAME: 'SET_NAME',
    CONFIGURE_LOBBY: 'CONFIGURE_LOBBY',
    CONFIGURE_LOBBY_RESPONSE: 'CONFIGURE_LOBBY_RESPONSE',
    CREATE_LOBBY: 'CREATE_LOBBY',
    CREATE_LOBBY_RESPONSE: 'CREATE_LOBBY_RESPONSE',
    JOIN_LOBBY: 'JOIN_LOBBY',
    JOIN_LOBBY_RESPONSE: 'JOIN_LOBBY_RESPONSE',
    JOIN_LOBBY_FAILED: 'JOIN_LOBBY_FAILED',
    LEAVE_LOBBY: 'LEAVE_LOBBY',
    LEAVE_LOBBY_RESPONSE: 'LEAVE_LOBBY_RESPONSE',
    LOBBY_UPDATE: 'LOBBY_UPDATE',
    START_GAME: 'START_GAME',
    START_GAME_RESPONSE: 'START_GAME_RESPONSE',
    UP: 'UP',
    DOWN: 'DOWN',
    LEFT: 'LEFT',
    RIGHT: 'RIGHT',
    PLAYER_POSITIONS: 'PLAYER_POSITIONS',
    ITEM_POSITIONS: 'ITEM_POSITIONS',
    KICK_PLAYER: 'KICK_PLAYER',
    KICK_PLAYER_RESPONSE: 'KICK_PLAYER_RESPONSE',
    PLAY_AGAIN: 'PLAY_AGAIN',
    GAME_STOPPED: 'GAME_STOPPED',
    NEXT_PLAYER_DEATH: 'NEXT_PLAYER_DEATH'
};

const ItemCode = {
    Apple: 'Apple'
};

const LobbyJoinFailureCodes = {
    SUCCESS: 'SUCCESS',
    FULL: 'FULL',
    NOT_EXISTING: 'NOT_EXISTING',
    STARTED: 'STARTED'
}
