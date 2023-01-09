var game_id_label_field;

function onLoadLobby() {
    game_id_label_field = document.querySelector(".game_id_label");
}
function startGame(){
    socket.send(new Message(OpCode.START_GAME).toJson());
    // is now round trip
}

function handleStartGameResponse(){
    lobby.style.display = "none";
    game.style.display = "contents";
}