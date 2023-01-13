var owner_id;
var username_input;
var username;
var game_id_input;
var game_id;
var debug_username;
var debug_game_id;
var debug_text;
var start_button;
var kick_buttons;
function onLoadIndex(){
    username_input = document.querySelector(".username");
    username_input.addEventListener('keyup', (evt) => { if(evt.key === "Enter") configureGame(); });
    game_id_input = document.querySelector(".game_id");
    game_id_input.addEventListener('keyup', (evt) => { if(evt.key === "Enter") joinGame(); });
    debug_username = document.querySelector(".debug_username");
    debug_game_id = document.querySelector(".debug_game_id");
    debug_text = document.querySelector(".debug_text");
    start_button = document.querySelector(".start_game");
    updateUsername();
    updateGameId();
}

function handleConnectionResponse(data){
    my_id = data.id;
}

function updateUsername(){
    username = username_input.value.trim();
    username_input.value = username;
    /* updates every time the input changes
    if (socket.readyState === WebSocket.OPEN) {
        socket.send(new Message(OpCode.SET_NAME, username).toJson())
    }
    */

}
function updateGameId(){
    game_id = game_id_input.value;
    if(!isNaN(game_id)){
        game_id = `#${game_id}`
    }
}

function configureGame(){
    if(username_check()){
        if (socket.readyState === WebSocket.OPEN) {
            socket.send(new Message(OpCode.SET_NAME, username).toJson())
        }
        socket.send(new Message(OpCode.CONFIGURE_LOBBY).toJson()) //redirect is now round trip
    }
}
function username_check(){
    debug_username.innerText = "";
    if(username.length === 0){
        debug_username.innerText = "enter a username";
        highlightElement(username_input);
        return false;
    }

    if(username.length > 10){
        debug_username.innerText = "username is to long";
        highlightElement(username_input);
        return false;
    }

    return true;
}

function game_id_check(){
    debug_game_id.innerText = "";
    if(!game_id.startsWith("#")){
        debug_game_id.innerText = "game id contains no letters";
        highlightElement(game_id_input);
        return false;
    }
    if(isNaN(game_id.substring(1))){
        debug_game_id.innerText = "game id contains no letters";
        highlightElement(game_id_input);
        return false;
    }
    if(game_id.length < 2){
        debug_game_id.innerText = "enter a game id";
        highlightElement(game_id_input);
        return false;
    }

    return true;
}
function handleConfigureGameResponse(msgContent){
    sLobby = msgContent
    index.style.display = "none";
    configure_game.style.display = "contents";
}


function joinGame(){
    if(!username_check()){
        return;
    }
    if(!game_id_check()){
        return;
    }

    socket.send(new Message(OpCode.SET_NAME, username).toJson());
    socket.send(new Message(OpCode.JOIN_LOBBY, game_id).toJson()); // is now round trip

}

function handleJoinGameResponse(msgContent){

    if (msgContent != null){
        sLobby = msgContent
        game_id_label_field.innerText = sLobby.ID;
        kick_buttons = document.querySelectorAll(".cross");
        owner_id = msgContent.owner.id;
        if(owner_id !== my_id){
            for (let button of kick_buttons) {
                button.style.display = "none";
            }
            start_button.style.display = "none";

        }

        // "redirect"
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