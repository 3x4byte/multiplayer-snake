var canvas;
var canvas_enemies;
var width;
var width_enemies;
var num_rows = 10;
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
    drawSnakes();
    updateInfobar();
}

function drawGrid(){
    // draw own game field
    let ctx = canvas.getContext("2d");
    ctx.beginPath();
    // drawing the grid
    for (let i = 0; i < width; i+= (width-1)/num_rows) {
        ctx.moveTo(0, i);
        ctx.lineTo(width, i);
        ctx.moveTo(i, 0);
        ctx.lineTo(i, width);
    }
    ctx.stroke();
    ctx.closePath();


    // draw Enemy game fields
    for(cv of canvas_enemies){
        ctx = cv.getContext("2d");
        ctx.beginPath();
        // drawing the grid
        for (let i = 0; i < width_enemies; i+= (width_enemies-1)/num_rows) {
            ctx.moveTo(0, i);
            ctx.lineTo(width_enemies, i);
            ctx.moveTo(i, 0);
            ctx.lineTo(i, width_enemies);
        }
        ctx.stroke();
        ctx.closePath();
    }
}

function drawSnakes(){
    // TODO
}

function updateInfobar(){
    // TODO
}

/*
let socket = new WebSocket("ws://localhost:5001");


// adding keydown EventListener for movement input of the User
document.addEventListener('keydown', keyInput);

function keyInput(evt){
    // whitelist of keys to be sent
    let key_filter = ['w', 'a', 's', 'd', 'W', 'A', 'S', 'D', "ArrowUp", "ArrowLeft", "ArrowDown", "ArrowRight"];
    let key_mapping = {
        'w': 'W',
        'W': 'W',
        'ArrowUp': 'W',
        'a' : 'A',
        'A' : 'A',
        'ArrowLeft': 'A',
        's': 'S',
        'S': 'S',
        'ArrowDown': 'S',
        'd': 'D',
        'D': 'D',
        'ArrowRight': 'D'
    }
    if(key_filter.includes(evt.key)){
        //console.log(key_mapping[evt.key]);
        socket.send(key_mapping[evt.key]);
    }
}

socket.onmessage = parseMessage;

function parseMessage(msg){

    // TODO
}
*/