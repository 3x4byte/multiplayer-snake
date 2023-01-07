var player_number_field;
var player_number;
var username;
var socket;

window.onload = windowLoaded;

function windowLoaded(){
    socket = new WebSocket("ws://localhost:5001");
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

    if(player_number > 8)
        return highlightElement(player_number_field);

    // TODO send data to server
    window.location.href = "lobby.html";
}

function highlightElement(element){
    // highlight the element
    element.classList.add("highlighted");
    // remove highlight after 3sec
    highlightElement.timer = setTimeout(() => element.classList.remove("highlighted"), 500);

}