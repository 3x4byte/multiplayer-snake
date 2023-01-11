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
        let joinedPlayer = document.createElement("li");
        joinedPlayer.setAttribute('id', player.id);
        let textnode = document.createTextNode(player.name);
        joinedPlayer.appendChild(textnode);
        list.appendChild(joinedPlayer);
    }

    adjustPlayerList();
}

function adjustPlayerList(){
    let longest_name = username.length;
    for(let name of enemy_names){
        if(name.length > longest_name){
            longest_name = name.length;
        }
    }

    player_list_element.style.width = `${longest_name*10}pt`;
}

function handleStartGameResponse(){
    for(let name of enemy_names){
        name_field_enemies[enemy_names.indexOf(name)].innerText = name;
    }
    name_field.innerText = username;

    lobby.style.display = "none";
    game.style.display = "contents";
}