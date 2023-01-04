let socket = new WebSocket("ws://localhost:5001");

// adding keydown EventListener for movement input of the User
document.addEventListener('keydown', keyInput);


function keyInput(evt){
    // whitelist of keys to be sent
    let key_filter = ['w', 'a', 's', 'd', 'W', 'A', 'S', 'D', "ArrowUp", "ArrowLeft", "ArrowDown", "ArrowRight"];
    let key_mapping = {
        'w': 'W',
        'W': 'W',
        'ArrowUp': 'W',
        'a' : 'A',
        'A' : 'A',
        'ArrowLeft': 'A',
        's': 'S',
        'S': 'S',
        'ArrowDown': 'S',
        'd': 'D',
        'D': 'D',
        'ArrowRight': 'D'
    }
    if(key_filter.includes(evt.key)){
        //console.log(key_mapping[evt.key]);
        socket.send(key_mapping[evt.key]);
    }
}