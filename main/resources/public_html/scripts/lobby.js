var game_id_label_field;
var player_list_element;
var enemy_names;
function onLoadLobby() {
    game_id_label_field = document.querySelector(".game_id_label");
    player_list_element = document.querySelector(".player_list");
}
function startGame(){
    socket.send(new Message(OpCode.START_GAME).toJson());
    // is now round trip
}

function handleLobbyUpdate(messageContent){
    let list = document.querySelector(".player_list");
    list.textContent = "";
    enemy_names = [];
    enemy_id = [];
    for (let player of messageContent) {
        if(player.id !== my_id) {
            enemy_names.push(player.name);
            enemy_id.push(player.id);
        }
        let div = document.createElement("div");
        let cross = document.createElement("img");
        cross.classList.add("cross");
        cross.id = player.id;
        cross.src = "../images/cross.png";
        cross.onmouseover = (evt) => { evt.srcElement.src = "../images/cross_hovered.png"; };
        cross.onmouseout = (evt) => { evt.srcElement.src = "../images/cross.png"; };
        cross.onclick = kick_player;
        let li = document.createElement("li");
        let playername = document.createTextNode(player.name);
        div.appendChild(cross);
        div.appendChild(playername);
        li.appendChild(div);
        list.appendChild(li);
    }
    adjustPlayerList();
}

function kick_player(msg){
    socket.send(new Message(OpCode.KICK_PLAYER, msg.srcElement.id).toJson());
}

function adjustPlayerList(){
    let longest_name = username.length;
    for(let name of enemy_names){
        if(name.length > longest_name){
            longest_name = name.length;
        }
    }

    player_list_element.style.width = `${longest_name*10 + 20}pt`;
}

function handleStartGameResponse(){
    for(let name of enemy_names){
        name_field_enemies[enemy_names.indexOf(name)].innerText = name;
    }
    name_field.innerText = username;

    lobby.style.display = "none";
    game.style.display = "contents";
}