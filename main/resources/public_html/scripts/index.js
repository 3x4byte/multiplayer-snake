var username_input;
var username;
var game_id_input;
var game_id;
window.onload = windowLoaded;

function windowLoaded(){
    username_input = document.querySelector(".username");
    game_id_input = document.querySelector(".game_id");
    updateUsername();
    updateGameId();
}
function updateUsername(){
    username = username_input.value;
}
function updateGameId(){
    game_id = game_id_input.value;
}
function createGame(){
    if(username.length > 0){
        window.location.href = "html/configure_game.html";
    }else{
        highlightElement(username_input);
    }
}

function joinGame(){
    if(username.length === 0){
        highlightElement(username_input);
    }else if(game_id.length === 0){
        highlightElement(game_id_input);
    }
    else{
        window.location.href = "html/configure_game.html";
    }
    // TODO send server join data
}

function highlightElement(element){
    // highlight the element
    console.log("highlighting...");
    element.classList.add("highlighted");
    // remove highlight after 3sec
    highlightElement.timer = setTimeout(() => element.classList.remove("highlighted"), 500);

}