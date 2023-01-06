/**
 * Represents a Message sent via Websocket
 */
export class Message {
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
export const OpCode = {
    ZERO: 'ZERO',
    CREATE_LOBBY: 'CREATE_LOBBY',
    JOIN_LOBBY: 'JOIN_LOBBY',
    JOIN_FAILED: 'JOIN_FAILED',
    LEAVE_LOBBY: 'LEAVE_LOBBY',
    UP: 'UP',
    DOWN: 'DOWN',
    LEFT: 'LEFT',
    RIGHT: 'RIGHT',
    PLAYER_POSITIONS: 'PLAYER_POSITIONS',
    ITEM_POSITIONS: 'ITEM_POSITIONS'
};
