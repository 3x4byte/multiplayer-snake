var socket;
var index;
var configure_game;
var lobby;
var game;

var sLobby;

window.onload = windowLoaded;

function windowLoaded(){
    socket = new WebSocket("ws://localhost:5001");
    index = document.querySelector(".index");
    configure_game = document.querySelector(".configure_game");
    lobby = document.querySelector(".lobby");
    game = document.querySelector(".game");

    onLoadIndex();
    onLoadConfigureGame();
    onLoadLobby();
    onLoadGame();

}


