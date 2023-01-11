var last_key_input = "UP";
var my_id;
var enemy_id;
var name_field;
var name_field_enemies;
var lives_field;
var lives_enemies_field;
var canvas;
var canvas_enemies;
var width;
var width_enemies;
var num_rows = 10;
var apples_coordinates;
var is_collided;

function onLoadGame(){
    socket.onmessage = handleMessage;

    name_field = document.querySelector(".own_name")
    name_field_enemies = document.querySelectorAll(".name")
    lives_field = document.querySelector(".own_lives")
    lives_enemies_field = document.querySelectorAll(".lives")
    canvas = document.querySelector(".own_game");
    canvas_enemies = document.querySelectorAll(".enemy");
    is_collided = false;
    windowResized();}

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

    //drawGrid();
}

function drawGrid(ctx, ctx_width){
    let tile_size = (ctx_width-1)/num_rows;
    ctx.beginPath();
    ctx.strokeStyle = "#00ADB5";
    // drawing the grid
    for (let i = 0; i < ctx_width; i+=tile_size) {
        ctx.moveTo(0, i);
        ctx.lineTo(ctx_width, i);
        ctx.moveTo(i, 0);
        ctx.lineTo(i, ctx_width);
    }
    ctx.stroke();
    ctx.closePath();
}

/*
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
*/

function updatePlayers(data){
    // loops all players
    for(let player of data){
        if(player !== null) {
            let id = player.id;
            let snake = player.snake;

            let ctx;
            if (id === my_id) {
                ctx = canvas.getContext("2d");
                ctx.clearRect(0, 0, width, width);
                drawApples(apples_coordinates, ctx, width);
                drawOwnSnake(ctx, snake.snakeFields);
                drawGrid(ctx, width);
            } else {
                ctx = canvas_enemies[enemy_id.indexOf(id)].getContext("2d");
                ctx.clearRect(0, 0, width_enemies, width_enemies);
                drawApples(apples_coordinates, ctx, width_enemies);
                drawSnake(ctx, snake.snakeFields);
                drawGrid(ctx, width_enemies);
            }
            updateLives(snake.lives, id);
            updateCollision(snake.collided, id);

        }
    }
}

function updateLives(lives, id){
    if(id === my_id){
        lives_field.innerHTML = `<img class="heart_img" src="../images/heart.png">`.repeat(lives);
    }else{
        lives_enemies_field[enemy_id.indexOf(id)].innerHTML = `<img class="heart_img" src="../images/heart.png">`.repeat(lives);
    }
}

function updateCollision(collided, id){
    is_collided = collided;
    if(is_collided){
        last_key_input = "UP";
    }
    // TODO maybe animation?
}

function updateItems(data){
    apples_coordinates = [];
    for(let item of data){
        let type = item[1];
        let x = item[0].x;
        let y = item[0].y;

        switch (type){
            case ItemCode.Apple: apples_coordinates.push([x,y]); break;

        }

    }
}

function drawApples(apples, ctx, ctx_width){
    let tile_size = (ctx_width-1)/num_rows;
    ctx.beginPath();
    ctx.fillStyle = "red";
    for(let apple of apples){
        ctx.ellipse(apple[0]*tile_size+(tile_size/2), apple[1]*tile_size+(tile_size/2), tile_size/3, tile_size/3, 0, 0, 360);
    }
    ctx.fill();
    ctx.closePath();

}

function drawOwnSnake(ctx, positions){
    let tile_size = (width-1)/num_rows;

    ctx.beginPath();
    // drawing head
    ctx.fillStyle = "darkgreen";
    let head = positions[0];
    ctx.fillRect(head.x*tile_size+tile_size/3, head.y*tile_size+tile_size/3, tile_size/3, tile_size/3);

    for(let i = 1; i < positions.length; i++){
        ctx.fillStyle = "green";

        // drawing center rect
        ctx.fillRect(positions[i].x*tile_size+tile_size/3, positions[i].y*tile_size+tile_size/3, tile_size/3, tile_size/3);

        //drawing connecting rect
        if(positions[i].x < positions[i-1].x){
            ctx.fillRect(positions[i].x*tile_size+tile_size/3*2, positions[i].y*tile_size+tile_size/3, tile_size/3, tile_size/3);
            ctx.fillRect(positions[i-1].x*tile_size, positions[i-1].y*tile_size+tile_size/3, tile_size/3, tile_size/3);

        }else if(positions[i].x > positions[i-1].x){
            ctx.fillRect(positions[i].x*tile_size, positions[i].y*tile_size+tile_size/3, tile_size/3, tile_size/3);
            ctx.fillRect(positions[i-1].x*tile_size+tile_size/3*2, positions[i-1].y*tile_size+tile_size/3, tile_size/3, tile_size/3);

        }else if(positions[i].y < positions[i-1].y){
            ctx.fillRect(positions[i].x*tile_size+tile_size/3, positions[i].y*tile_size+tile_size/3*2, tile_size/3, tile_size/3);
            ctx.fillRect(positions[i-1].x*tile_size+tile_size/3, positions[i-1].y*tile_size, tile_size/3, tile_size/3);

        }else if(positions[i].y > positions[i-1].y) {
            ctx.fillRect(positions[i].x*tile_size+tile_size/3, positions[i].y*tile_size, tile_size/3, tile_size/3);
            ctx.fillRect(positions[i-1].x*tile_size+tile_size/3, positions[i-1].y*tile_size+tile_size/3*2, tile_size/3, tile_size/3);

        }
    }
    ctx.fill();
    ctx.closePath();

}


function drawSnake(ctx, positions){
    let tile_size = (width_enemies-1)/num_rows;

    let head = true;
    ctx.beginPath();
    // loops over the tail coordinates
    for(let position of positions){
        ctx.fillStyle = "green";
        if(head){
            ctx.fillStyle = "darkgreen";
            head = false;
        }
        ctx.fillRect(position.x * tile_size, position.y * tile_size, tile_size, tile_size);
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

        if(game.style.display === "none") {
            return;
        }
        if(is_collided === true) {
            return;
        }
        if (msg.opCode === last_key_input) {
            return;
        }

        last_key_input = msg.opCode;
        socket.send(msg.toJson());

    }
}

function handleMessage(websocketMessage){
    //console.log("Incoming message: " + websocketMessage.data); //only delete after debugging please
    let message = Message.fromJson(websocketMessage.data);
    //console.log("Parsed message: ", message); //only delete after debugging please

    switch (message.opCode){
        case OpCode.PLAYER_POSITIONS: updatePlayers(message.content); break;
        case OpCode.ITEM_POSITIONS: updateItems(message.content); break;
        case OpCode.CONFIGURE_LOBBY_RESPONSE: handleConfigureGameResponse(message.content); break;
        case OpCode.CREATE_LOBBY_RESPONSE: handleCreateLobbyResponse(message.content); break;
        case OpCode.JOIN_LOBBY_RESPONSE: handleJoinGameResponse(message.content); break;
        case OpCode.START_GAME_RESPONSE: handleStartGameResponse(); break;
        case OpCode.LOBBY_UPDATE: handleLobbyUpdate(message.content); break;
        case OpCode.CONNECTION_RESPONSE: handleConnectionResponse(message.content); break;
    }
}

