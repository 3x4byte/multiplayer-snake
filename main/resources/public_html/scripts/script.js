var socket;
var index;
var configure_game;
var lobby;
var game;
var game_over;

var sLobby;

window.onload = windowLoaded;

function windowLoaded(){
    socket = new WebSocket(`ws://${location.hostname}:5001`);
    index = document.querySelector(".index");
    configure_game = document.querySelector(".configure_game");
    lobby = document.querySelector(".lobby");
    game = document.querySelector(".game");
    game_over = document.querySelector(".game_over");

    onLoadIndex();
    onLoadConfigureGame();
    onLoadLobby();
    onLoadGame();

}


