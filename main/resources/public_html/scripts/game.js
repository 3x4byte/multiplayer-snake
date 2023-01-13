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
var apple_img = new Image();
var head_img_1 = new Image();
var head_img_2 = new Image();
var head_img_3 = new Image();
var head_img_4 = new Image();
var straight_img_1 = new Image();
var straight_img_2 = new Image();
var corner_img_1 = new Image();
var corner_img_2 = new Image();
var corner_img_3 = new Image();
var corner_img_4 = new Image();
var tail_img_1 = new Image();
var tail_img_2 = new Image();
var tail_img_3 = new Image();
var tail_img_4 = new Image();

function onLoadGame(){
    socket.onmessage = handleMessage;

    name_field = document.querySelector(".own_name")
    name_field_enemies = document.querySelectorAll(".name")
    lives_field = document.querySelector(".own_lives")
    lives_enemies_field = document.querySelectorAll(".lives")
    canvas = document.querySelector(".own_game");
    canvas_enemies = document.querySelectorAll(".enemy");
    apple_img.src = "../images/apple.png";
    head_img_1.src = "../images/head_1.png";
    head_img_2.src = "../images/head_2.png";
    head_img_3.src = "../images/head_3.png";
    head_img_4.src = "../images/head_4.png";
    straight_img_1.src = "../images/straight_1.png";
    straight_img_2.src = "../images/straight_2.png";
    corner_img_1.src = "../images/corner_1.png";
    corner_img_2.src = "../images/corner_2.png";
    corner_img_3.src = "../images/corner_3.png";
    corner_img_4.src = "../images/corner_4.png";
    tail_img_1.src = "../images/tail_1.png";
    tail_img_2.src = "../images/tail_2.png";
    tail_img_3.src = "../images/tail_3.png";
    tail_img_4.src = "../images/tail_4.png";
    is_collided = false;
    windowResized();
}

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

function updatePlayers(data){
    // loops all players
    for(let player of data) {
        if (player !== null) {
            let id = player.id;
            let snake = player.snake;
            if (snake.lives > 0) {

                let ctx;
                if (id === my_id) {
                    ctx = canvas.getContext("2d");
                    ctx.clearRect(0, 0, width, width);
                    drawApples(apples_coordinates, ctx, width);
                    drawSnake(ctx, snake.snakeFields, width);
                    drawGrid(ctx, width);
                } else {
                    ctx = canvas_enemies[enemy_id.indexOf(id)].getContext("2d");
                    ctx.clearRect(0, 0, width_enemies, width_enemies);
                    drawApples(apples_coordinates, ctx, width_enemies);
                    drawSnake(ctx, snake.snakeFields, width_enemies);
                    drawGrid(ctx, width_enemies);
                }
            }
            else{
                let size;
                let pos;
                if (id === my_id) {
                    ctx = canvas.getContext("2d");
                    size = width / 10;
                    pos = width / 2;
                }else{
                    ctx = canvas_enemies[enemy_id.indexOf(id)].getContext("2d");
                    size = width_enemies/10;
                    pos = width_enemies / 2;
                }
                ctx.font = `${size}pt panton`;
                ctx.textAlign = "center";
                ctx.fillStyle = "orange";
                ctx.fillText("game over", pos, pos);

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

    ctx.imageSmoothingEnabled = false;

    for(let apple of apples) {
        ctx.drawImage(apple_img, apple[0]*tile_size+(tile_size*0.1), apple[1]*tile_size+(tile_size*0.1), tile_size*0.8, tile_size*0.8);
    }


}

function drawSnake(ctx, positions, width){
    let tile_size = (width-1)/num_rows;

    ctx.imageSmoothingEnabled = false;

    for (let i = 1; i < positions.length-1; i++) {

        if(positions[i-1].x > positions[i].x && positions[i].x > positions[i+1].x || positions[i-1].x < positions[i].x && positions[i].x < positions[i+1].x){
            ctx.drawImage(straight_img_2, positions[i].x*tile_size, positions[i].y*tile_size, tile_size, tile_size);
        }
        else if(positions[i-1].y > positions[i].y && positions[i].y > positions[i+1].y || positions[i-1].y < positions[i].y && positions[i].y < positions[i+1].y){
            ctx.drawImage(straight_img_1, positions[i].x*tile_size, positions[i].y*tile_size, tile_size, tile_size);
        }
        else if(positions[i-1].y < positions[i].y && positions[i].x < positions[i+1].x || positions[i-1].x > positions[i].x && positions[i].y > positions[i+1].y){
            ctx.drawImage(corner_img_1, positions[i].x*tile_size, positions[i].y*tile_size, tile_size, tile_size);
        }
        else if(positions[i-1].x > positions[i].x && positions[i].y < positions[i+1].y || positions[i-1].y > positions[i].y && positions[i].x < positions[i+1].x){
            ctx.drawImage(corner_img_2, positions[i].x*tile_size, positions[i].y*tile_size, tile_size, tile_size);
        }
        else if(positions[i-1].y > positions[i].y && positions[i].x > positions[i+1].x || positions[i-1].x < positions[i].x && positions[i].y < positions[i+1].y){
            ctx.drawImage(corner_img_3, positions[i].x*tile_size, positions[i].y*tile_size, tile_size, tile_size);
        }
        else if(positions[i-1].x < positions[i].x && positions[i].y > positions[i+1].y || positions[i-1].y < positions[i].y && positions[i].x > positions[i+1].x){
            ctx.drawImage(corner_img_4, positions[i].x*tile_size, positions[i].y*tile_size, tile_size, tile_size);
        }
    }

    let before = positions[positions.length-2];
    let tail = positions[positions.length-1];

    if(before.x > tail.x){
        ctx.drawImage(tail_img_2, tail.x*tile_size, tail.y*tile_size, tile_size, tile_size);
    }
    else if(before.x < tail.x){
        ctx.drawImage(tail_img_4, tail.x*tile_size, tail.y*tile_size, tile_size, tile_size);
    }
    else if(before.y > tail.y){
        ctx.drawImage(tail_img_3, tail.x*tile_size, tail.y*tile_size, tile_size, tile_size);
    }
    else if(before.y < tail.y){
        ctx.drawImage(tail_img_1, tail.x*tile_size, tail.y*tile_size, tile_size, tile_size);
    }

    let head = positions[0];
    let next = positions[1];

    if(head.x > next.x){
        ctx.drawImage(head_img_2, head.x*tile_size, head.y*tile_size, tile_size, tile_size);
    }
    else if(head.x < next.x){
        ctx.drawImage(head_img_4, head.x*tile_size, head.y*tile_size, tile_size, tile_size);
    }
    else if(head.y > next.y){
        ctx.drawImage(head_img_3, head.x*tile_size, head.y*tile_size, tile_size, tile_size);
    }
    else if(head.y < next.y){
        ctx.drawImage(head_img_1, head.x*tile_size, head.y*tile_size, tile_size, tile_size);
    }

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
    console.log("Incoming message: " + websocketMessage.data); //only delete after debugging please
    let message = Message.fromJson(websocketMessage.data);
    console.log("Parsed message: ", message); //only delete after debugging please

    switch (message.opCode){
        case OpCode.PLAYER_POSITIONS: updatePlayers(message.content); break;
        case OpCode.ITEM_POSITIONS: updateItems(message.content); break;
        case OpCode.CONFIGURE_LOBBY_RESPONSE: handleConfigureGameResponse(message.content); break;
        case OpCode.CREATE_LOBBY_RESPONSE: handleCreateLobbyResponse(message.content); break;
        case OpCode.JOIN_LOBBY_RESPONSE: handleJoinGameResponse(message.content); break;
        case OpCode.START_GAME_RESPONSE: handleStartGameResponse(); break;
        case OpCode.LOBBY_UPDATE: handleLobbyUpdate(message.content); break;
        case OpCode.CONNECTION_RESPONSE: handleConnectionResponse(message.content); break;
        case OpCode.KICK_PLAYER_RESPONSE: handleKickPlayerResponse(); break;
    }
}

