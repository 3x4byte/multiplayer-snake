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

    for (let player in messageContent.members) {
        if(messageContent.members[player].id !== my_id) {
            enemy_names.push(messageContent.members[player].name);
            enemy_id.push(messageContent.members[player].id);
        }

        let div = document.createElement("div");
        let cross = document.createElement("img");

        if(my_id === messageContent.owner.id){
            if(player !== my_id) {
                cross.classList.add("cross");
                cross.id = messageContent.members[player].id;
                cross.src = "../images/cross.png";
                cross.onmouseover = (evt) => { evt.srcElement.src = "../images/cross_hovered.png"; };
                cross.onmouseout = (evt) => { evt.srcElement.src = "../images/cross.png"; };
                cross.onclick = kick_player;
            }
        }

        let li = document.createElement("li");
        let playername = document.createTextNode(messageContent.members[player].name);
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
    is_game_over = false;
    for(let name of enemy_names){
        name_field_enemies[enemy_names.indexOf(name)].innerText = name;
    }
    name_field.innerText = username;

    index.style.display = "none";
    configure_game.style.display = "none";
    lobby.style.display = "none";
    game.style.display = "contents";
    game_over.style.display = "none";

}

function handleKickPlayerResponse(){
    debug_text.innerText = "you have been kicked!";

    index.style.display = "contents";
    configure_game.style.display = "none";
    lobby.style.display = "none";
    game.style.display = "none";
    game_over.style.display = "none";

}