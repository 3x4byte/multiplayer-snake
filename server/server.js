// Server shit
const path = require("path");
const express = require("express");
const portnumber = 5000;
const server = express();
server.use(express.static(path.join(__dirname+"/../public_html", ".")));
server.use(express.urlencoded({ extended: false, limit: "1mb"}));
server.use(express.json());
server.set("view engine", "ejs");
server.listen(portnumber, function(){
    console.log(`listening at port: ${portnumber}`)
});

//websocket shit
const websocket_package = require("ws");
const websocket_portnumber = 5001;
const serverSocket = new websocket_package.Server({ port: websocket_portnumber});
let clientAmt = 0;

//snkae shit
let snake = []


serverSocket.on("connection", function (socket) {
    let clientNr = clientAmt;
    snake[clientNr] = new Snake();
    clientAmt++;
    console.log(`client ${clientNr} accepted`);

    socket.onmessage = function (event){
        snake[clientNr].move(event.data);
        console.log(snake[clientNr].getHead())
        console.log(snake[clientNr].getTail())
        let ret = parseData(snake[clientNr].getHead(), snake[clientNr].getTail());
        let ret_json = JSON.stringify(ret);
        socket.send(ret_json);
    }

    socket.onclose = function(event){
        console.log(`${clientNr} closed the connection`);
    }

});

function parseData(head, tail){
    ret = [];
    ret.push({type: "rectangle", x: head.x, y: head.y, fillColor: "darkgreen"});
    for(element of tail){
        ret.push({type: "rectangle", x: element.x, y: element.y, fillColor: "green"});
    }
    return ret;
}



/////////// Snake Class //////////////////
class Snake{
    constructor(){
        this.length = 3;
        this.head = {x: 8, y: 8}
        this.tail = [{x: 8, y: 10}, {x: 8, y: 9}]
        this.direction = {x: 0, y: -1}
    }

    getHead(){
        return this.head
    }

    getTail(){
        return this.tail
    }

    updatePos(){
        this.tail.push(JSON.parse(JSON.stringify(this.head)));
        if(this.tail.length > this.length){
            this.tail.shift();
        }

        this.head.x += this.direction.x;
        this.head.y += this.direction.y;

    }

    move(key){
        if((key == 'w' || key == 'ArrowUp') && this.direction.y != 1){
            this.direction = {x: 0, y: -1};
        }
        if((key == 's' || key == 'ArrowDown') && this.direction.y != -1){
            this.direction = {x: 0, y: 1};
        }
        if((key == 'a' || key == 'ArrowLeft') && this.direction.x != 1){
            this.direction = {x: -1, y: 0};
        }
        if((key == 'd' || key == 'ArrowRight') && this.direction.x != -1){
            this.direction = {x: 1, y: 0};
        }

        this.updatePos();
    }

    

}