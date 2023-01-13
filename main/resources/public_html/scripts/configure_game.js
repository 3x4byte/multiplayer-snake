var player_number_field;
var player_number;

function onLoadConfigureGame(){
    player_number_field = document.querySelector(".player_number");
    player_number = player_number_field.value;
}
function updatePlayerNumber(){
    player_number = parseInt(player_number_field.value);
}

function createLobby(){
    if(isNaN(player_number))
        return highlightElement(player_number_field);

    if(player_number < 4)
        return highlightElement(player_number_field);

    if(player_number > 9)
        return highlightElement(player_number_field);

    // "redirect"
    sLobby.lobbySize = String(player_number);
    socket.send(new Message(OpCode.CREATE_LOBBY, sLobby).toJson()); //is now a round trip
}

function handleCreateLobbyResponse(msgContent){
    sLobby = msgContent;

    index.style.display = "none";
    configure_game.style.display = "none";
    lobby.style.display = "contents";
    game.style.display = "none";
    game_over.style.display = "none";
    // displaying lobby id
    game_id_label_field.innerText = msgContent.ID;
}

function highlightElement(element){
    // highlight the element
    element.classList.add("highlighted");
    // remove highlight after 3sec
    highlightElement.timer = setTimeout(() => element.classList.remove("highlighted"), 500);

}