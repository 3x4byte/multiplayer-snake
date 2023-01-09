//region global

var index;
var configure_game;
var lobby;
var game;
var sLobby;

window.onload = windowLoaded;

function windowLoaded(){
    // global
    socket = new WebSocket("ws://localhost:5001");
    index = document.querySelector(".index");
    configure_game = document.querySelector(".configure_game");
    lobby = document.querySelector(".lobby");
    game = document.querySelector(".game");
    //------------------------

    // index
    username_input = document.querySelector(".username");
    game_id_input = document.querySelector(".game_id");
    updateUsername();
    updateGameId();
    //------------------------

    //configure game
    player_number_field = document.querySelector(".player_number");
    player_number = player_number_field.value;
    //------------------------

    // lobby
    game_id_label_field = document.querySelector(".game_id_label");
    //------------------------

    //game
    socket.onmessage = handleMessage;

    name_field = document.querySelector(".own_name")
    name_enemies_field = document.querySelectorAll(".name")
    lives_field = document.querySelector(".own_lives")
    lives_enemies_field = document.querySelectorAll(".lives")
    canvas = document.querySelector(".own_game");
    canvas_enemies = document.querySelectorAll(".enemy");
    windowResized();
    //------------------------
}
//endregion

//region index
var username_input;
var username;
var game_id_input;
var game_id;

function updateUsername(){
    username = username_input.value;
    /* updates every time the input changes
    if (socket.readyState === WebSocket.OPEN) {
        socket.send(new Message(OpCode.SET_NAME, username).toJson())
    }
    */

}
function updateGameId(){
    game_id = game_id_input.value;
}

function configureGame(){
    if(username.length > 0){
        localStorage.setItem("username", username);

        // "redirect - "
        if (socket.readyState === WebSocket.OPEN) {
            socket.send(new Message(OpCode.SET_NAME, username).toJson())
        }
        socket.send(new Message(OpCode.CONFIGURE_LOBBY).toJson()) //redirect is now round trip
    }else{
        highlightElement(username_input);
    }
}

function handleConfigureGameResponse(msgContent){
    sLobby = msgContent
    index.style.display = "none";
    configure_game.style.display = "contents";
}


function joinGame(){
    if(username.length === 0){
        highlightElement(username_input);
    }else if(game_id.length === 0){
        highlightElement(game_id_input);
    }
    else{

        // "redirect"
        socket.send(new Message(OpCode.SET_NAME, username).toJson());
        socket.send(new Message(OpCode.JOIN_LOBBY, game_id).toJson()); // is now round trip
    }
}

function handleJoinGameResponse(msgContent){
    if (msgContent != null){
        sLobby = msgContent
        index.style.display = "none";
        lobby.style.display = "contents";
    }
}


function highlightElement(element){
    // highlight the element
    element.classList.add("highlighted");
    // remove highlight after 3sec
    highlightElement.timer = setTimeout(() => element.classList.remove("highlighted"), 500);

}
//endregion

//region configure_game

var player_number_field;
var player_number;


function updatePlayerNumber(){
    player_number = parseInt(player_number_field.value);
}

function createLobby(){
    if(isNaN(player_number))
        return highlightElement(player_number_field);

    if(player_number < 4)
        return highlightElement(player_number_field);

    if(player_number > 8)
        return highlightElement(player_number_field);

    // "redirect"
    sLobby.lobbySize = String(player_number);
    socket.send(new Message(OpCode.CREATE_LOBBY, sLobby).toJson()); //is now a round trip
}

function handleCreateLobbyResponse(msgContent){
    sLobby = msgContent;
    configure_game.style.display = "none";
    lobby.style.display = "contents";

    // displaying lobby id
    game_id_label_field.innerText = msgContent.ID;
}

function highlightElement(element){
    // highlight the element
    element.classList.add("highlighted");
    // remove highlight after 3sec
    highlightElement.timer = setTimeout(() => element.classList.remove("highlighted"), 500);

}
//endregion

//region lobby

var game_id_label_field;

function startGame(){
    socket.send(new Message(OpCode.START_GAME).toJson());
    // is now round trip
}

function handleStartGameResponse(){
    lobby.style.display = "none";
    game.style.display = "contents";
}
//endregion

//region game

var socket;

var last_key_input = "UP";

var pid = 0; // TODO get own id from server
let own_canvas_found;
var name_field;
var name_enemies_field;
var lives_field;
var lives_enemies_field;
var canvas;
var canvas_enemies;
var width;
var width_enemies;
var num_rows = 10;
var tile_size;
var tile_size_enemy;
var apples_coordinates;


// adding resize event to scale the canvases
window.addEventListener("resize", windowResized);

// adding keydown EventListener for movement input of the User
document.addEventListener('keydown', keyInput);

function windowResized(){
    width = window.innerWidth * 0.34;
    canvas.setAttribute("height", width);
    canvas.setAttribute("width", width);

    width_enemies = width * 0.2;
    for(let c of canvas_enemies){
        c.setAttribute("height", width_enemies);
        c.setAttribute("width", width_enemies);
    }

    drawGrid();
}

function drawGrid(){
    tile_size = (width-1)/num_rows;
    // draw own game field
    let ctx = canvas.getContext("2d");
    ctx.beginPath();
    ctx.strokeStyle = "#00ADB5";
    // drawing the grid
    for (let i = 0; i < width; i+=tile_size) {
        ctx.moveTo(0, i);
        ctx.lineTo(width, i);
        ctx.moveTo(i, 0);
        ctx.lineTo(i, width);
    }
    ctx.stroke();
    ctx.closePath();

    tile_size_enemy = (width_enemies-1)/num_rows
    // draw Enemy game fields
    for(let cv of canvas_enemies){
        ctx = cv.getContext("2d");
        ctx.beginPath();
        ctx.strokeStyle = "#00ADB5";
        // drawing the grid
        for (let i = 0; i < width_enemies; i+=tile_size_enemy) {
            ctx.moveTo(0, i);
            ctx.lineTo(width_enemies, i);
            ctx.moveTo(i, 0);
            ctx.lineTo(i, width_enemies);
        }
        ctx.stroke();
        ctx.closePath();
    }
}

function updatePlayers(data){

    own_canvas_found = 0;
    // loops all players
    for(let player of data){
        let id = player.id;
        let snake = player.snake;

        let ctx;
        if(id === pid){
            own_canvas_found = 1;
            ctx = canvas.getContext("2d");
            ctx.clearRect(0, 0, tile_size*num_rows, tile_size*num_rows);
            drawApples(apples_coordinates, ctx, tile_size);
            drawOwnSnake(ctx, snake.snakeFields, tile_size);
        }
        else{
            ctx = canvas_enemies[id-own_canvas_found].getContext("2d");
            ctx.clearRect(0, 0, tile_size_enemy*num_rows, tile_size_enemy*num_rows);
            drawApples(apples_coordinates, ctx, tile_size_enemy);
            drawSnake(ctx, snake.snakeFields, tile_size_enemy);
        }
        updateLives(snake.lives, id);
        updateCollision(snake.collided, id);

    }
    drawGrid();
}

function updateLives(lives, id){
    if(id === pid){
        lives_field.innerHTML = `<img class="heart_img" src="../images/heart.png">`.repeat(lives);
    }else{
        lives_enemies_field[id - own_canvas_found].innerHTML = `<img class="heart_img" src="../images/heart.png">`.repeat(lives);
    }
}

function updateCollision(is_collided, id){
    // TODO maybe animation?
}

function updateItems(data){
    apples_coordinates = [];
    for(let item of data){
        let type = item[1];
        let x = item[0].x;
        let y = item[0].y;

        switch (type){
            // case ItemCode.Apple: drawApple(x, y); break;
            case ItemCode.Apple: apples_coordinates.push([x,y]); break;

        }

    }
}

function drawApples(apples, ctx, size){
    ctx.beginPath();
    ctx.fillStyle = "red";
    for(let apple of apples){
        ctx.ellipse(apple[0]*size+(size/2), apple[1]*size+(size/2), size/3, size/3, 0, 0, 360);
    }
    ctx.fill();
    ctx.closePath();

}

function drawOwnSnake(ctx, positions, size){

    ctx.beginPath();
    // drawing head
    ctx.fillStyle = "darkgreen";
    let head = positions[0];
    ctx.fillRect(head.x*size+size/3, head.y*size+size/3, size/3, size/3);

    for(let i = 1; i < positions.length; i++){
        ctx.fillStyle = "green";

        // drawing center rect
        ctx.fillRect(positions[i].x*size+size/3, positions[i].y*size+size/3, size/3, size/3);

        //drawing connecting rect
        if(positions[i].x < positions[i-1].x){
            ctx.fillRect(positions[i].x*size+size/3*2, positions[i].y*size+size/3, size/3, size/3);
            ctx.fillRect(positions[i-1].x*size, positions[i-1].y*size+size/3, size/3, size/3);

        }else if(positions[i].x > positions[i-1].x){
            ctx.fillRect(positions[i].x*size, positions[i].y*size+size/3, size/3, size/3);
            ctx.fillRect(positions[i-1].x*size+size/3*2, positions[i-1].y*size+size/3, size/3, size/3);

        }else if(positions[i].y < positions[i-1].y){
            ctx.fillRect(positions[i].x*size+size/3, positions[i].y*size+size/3*2, size/3, size/3);
            ctx.fillRect(positions[i-1].x*size+size/3, positions[i-1].y*size, size/3, size/3);

        }else if(positions[i].y > positions[i-1].y) {
            ctx.fillRect(positions[i].x*size+size/3, positions[i].y*size, size/3, size/3);
            ctx.fillRect(positions[i-1].x*size+size/3, positions[i-1].y*size+size/3*2, size/3, size/3);

        }
    }
    ctx.fill();
    ctx.closePath();

}


function drawSnake(ctx, positions, size){
    let head = true;
    ctx.beginPath();
    // loops over the tail coordinates
    for(let position of positions){
        ctx.fillStyle = "green";
        if(head){
            ctx.fillStyle = "darkgreen";
            head = false;
        }
        ctx.fillRect(position.x * size, position.y * size, size, size);
    }

    ctx.fill();
    ctx.closePath();
}


function keyInput(evt){
    // whitelist of keys to be sent
    let key_filter = ['w', 'a', 's', 'd', 'W', 'A', 'S', 'D', "ArrowUp", "ArrowLeft", "ArrowDown", "ArrowRight"];
    let key_mapping = {
        'w': OpCode.UP,
        'W': OpCode.UP,
        'ArrowUp': OpCode.UP,
        'a' : OpCode.LEFT,
        'A' : OpCode.LEFT,
        'ArrowLeft': OpCode.LEFT,
        's': OpCode.DOWN,
        'S':  OpCode.DOWN,
        'ArrowDown':  OpCode.DOWN,
        'd': OpCode.RIGHT,
        'D': OpCode.RIGHT,
        'ArrowRight': OpCode.RIGHT
    }
    if(key_filter.includes(evt.key)){
        let msg = new Message(key_mapping[evt.key]);
        if(game.style.display !== "none") {
            if (msg.opCode !== last_key_input) {
                last_key_input = msg.opCode;
                socket.send(msg.toJson());
            }
        }

    }
}

function handleMessage(websocketMessage){
    //console.log("Incoming message: " + websocketMessage.data); //only delete after debugging please
    let message = Message.fromJson(websocketMessage.data);
    //console.log("Parsed message: " + message); //only delete after debugging please

    switch (message.opCode){
        case OpCode.PLAYER_POSITIONS: updatePlayers(message.content); break;
        case OpCode.ITEM_POSITIONS: updateItems(message.content); break;
        case OpCode.CONFIGURE_LOBBY_RESPONSE: handleConfigureGameResponse(message.content); break;
        case OpCode.CREATE_LOBBY_RESPONSE: handleCreateLobbyResponse(message.content); break
        case OpCode.JOIN_LOBBY_RESPONSE: handleJoinGameResponse(message.content); break
        case OpCode.START_GAME_RESPONSE: handleStartGameResponse(); break
    }
}


//endregion

//region communication
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
    SET_NAME: 'SET_NAME',
    CONFIGURE_LOBBY: 'CONFIGURE_LOBBY',
    CONFIGURE_LOBBY_RESPONSE: 'CONFIGURE_LOBBY_RESPONSE',
    CREATE_LOBBY: 'CREATE_LOBBY',
    CREATE_LOBBY_RESPONSE: 'CREATE_LOBBY_RESPONSE',
    JOIN_LOBBY: 'JOIN_LOBBY',
    JOIN_LOBBY_RESPONSE: 'JOIN_LOBBY_RESPONSE',
    LEAVE_LOBBY: 'LEAVE_LOBBY',
    START_GAME: 'START_GAME',
    START_GAME_RESPONSE: 'START_GAME_RESPONSE',
    UP: 'UP',
    DOWN: 'DOWN',
    LEFT: 'LEFT',
    RIGHT: 'RIGHT',
    PLAYER_POSITIONS: 'PLAYER_POSITIONS',
    ITEM_POSITIONS: 'ITEM_POSITIONS'
};

const ItemCode = {
    Apple: 'Apple'
};

//endregion