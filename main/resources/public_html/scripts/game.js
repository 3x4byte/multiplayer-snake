import { Message, OpCode } from '/scripts/communication.js';

var canvas;
var canvas_enemies;
var width;
var width_enemies;
var num_rows = 10;
var tile_size;
var tile_size_enemy;
window.onload = windowLoaded;

function windowLoaded(){
    // TODO connectToWebsocket();

    canvas = document.querySelector(".own_game");
    canvas_enemies = document.querySelectorAll(".enemy");

    windowResized();
}

window.addEventListener("resize", windowResized);

function windowResized(evt){
    width = window.innerWidth * 0.34;
    canvas.setAttribute("height", width);
    canvas.setAttribute("width", width);

    width_enemies = width * 0.2;
    for(let c of canvas_enemies){
        c.setAttribute("height", width_enemies);
        c.setAttribute("width", width_enemies);
    }

    drawCanvas();
}

function drawCanvas(){
    drawGrid();
    updateInfobar();
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

function drawSnakes(positions){
    let ctx = canvas.getContext("2d"); // TODO get corresponding canvas

    ctx.clearRect(0, 0, width, width);

    // loops all players
    for(let player of positions){

        let id = player.shift();
        let head = player.shift();

        ctx.beginPath();
        // draw head of snake
        ctx.fillStyle = "darkgreen";
        ctx.fillRect(head[0] * tile_size, head[1] * tile_size, tile_size, tile_size);

        // loops over the tail coordinates
        for(let positions of player){
            ctx.fillStyle = "green";
            ctx.fillRect(positions[0] * tile_size, positions[1] * tile_size, tile_size, tile_size);
        }

        ctx.fill();
        ctx.closePath();

    }

    drawGrid();
}

function updateInfobar(){
    // TODO
}

let socket = new WebSocket("ws://localhost:5001");


// adding keydown EventListener for movement input of the User
document.addEventListener('keydown', keyInput);

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
        let msg = new Message(key_mapping[evt.key])
        socket.send(msg.toJson());
    }
}

socket.onmessage = handleMessage;

function handleMessage(websocketMessage){
    console.log("Incoming message: " + websocketMessage.data) //only delete after debugging please
    let message = Message.fromJson(websocketMessage.data)
    console.log("Parsed message: " + message) //only delete after debugging please


    switch (message.opCode){
        case OpCode.PLAYER_POSITIONS: drawSnakes(message.content); break;
    }

}
