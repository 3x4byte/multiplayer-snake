var username_input;
var username;
var game_id_input;
var game_id;
function onLoadIndex(){
    username_input = document.querySelector(".username");
    game_id_input = document.querySelector(".game_id");
    updateUsername();
    updateGameId();
}

function updateUsername(){
    username = username_input.value;
    /* updates every time the input changes
    if (socket.readyState === WebSocket.OPEN) {
        socket.send(new Message(OpCode.SET_NAME, username).toJson())
    }
    */

}
function updateGameId(){
    game_id = game_id_input.value;
}

function configureGame(){
    if(username.length > 0){
        localStorage.setItem("username", username);

        // "redirect - "
        if (socket.readyState === WebSocket.OPEN) {
            socket.send(new Message(OpCode.SET_NAME, username).toJson())
        }
        socket.send(new Message(OpCode.CONFIGURE_LOBBY).toJson()) //redirect is now round trip
    }else{
        highlightElement(username_input);
    }
}

function handleConfigureGameResponse(msgContent){
    sLobby = msgContent
    index.style.display = "none";
    configure_game.style.display = "contents";
}


function joinGame(){
    if(username.length === 0){
        highlightElement(username_input);
    }else if(game_id.length === 0){
        highlightElement(game_id_input);
    }
    else{

        // "redirect"
        socket.send(new Message(OpCode.SET_NAME, username).toJson());
        socket.send(new Message(OpCode.JOIN_LOBBY, game_id).toJson()); // is now round trip
    }
}

function handleJoinGameResponse(msgContent){
    if (msgContent != null){
        sLobby = msgContent
        index.style.display = "none";
        lobby.style.display = "contents";
    }
}


function highlightElement(element) {
    // highlight the element
    element.classList.add("highlighted");
    // remove highlight after 3sec
    highlightElement.timer = setTimeout(() => element.classList.remove("highlighted"), 500);
}