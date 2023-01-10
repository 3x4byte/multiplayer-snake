var game_id_label_field;

function onLoadLobby() {
    game_id_label_field = document.querySelector(".game_id_label");
}
function startGame(){
    socket.send(new Message(OpCode.START_GAME).toJson());
    // is now round trip
}

function handleLobbyUpdate(messageContent){
    var list = document.getElementById("player_list");
    list.textContent = "";

    var i = 1;
    for (var player of messageContent) {
        var joinedPlayer = document.createElement("li");
        joinedPlayer.setAttribute('id', player.id);
        var textnode = document.createTextNode(i++ + " " + player.name);
        joinedPlayer.appendChild(textnode);
        list.appendChild(joinedPlayer);
    }
}

function handleStartGameResponse(){
    lobby.style.display = "none";
    game.style.display = "contents";
}