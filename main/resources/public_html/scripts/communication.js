export class Message {
    constructor(opCode, content) {
        this.opCode = opCode;
        this.content = content;
    }

    static fromJson(jsonString) {
        const obj = JSON.parse(jsonString);
        return new Message(obj.opCode, obj.content);
    }

    toJson() {
        return JSON.stringify({
            opCode: this.opCode,
            content: this.content
        });
    }
}

export const OpCode = {
    ZERO: 'UNIQUE',
    CREATE_LOBBY: 'CREATE',
    JOIN_LOBBY: 'JOIN',
    JOIN_FAILED: 'JOIN_FAILED',
    LEAVE_LOBBY: 'LEAVE',
    UP: 'W',
    DOWN: 'S',
    LEFT: 'A',
    RIGHT: 'D',
    PLAYER_POSITIONS: 'POSITIONS'
};

//todo IMPORT intead of copy (however that works in this shitty language)