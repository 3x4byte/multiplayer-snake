const path = require("path");
const express = require("express");
const portnumber = 5000;
const server = express();
server.use(express.static(path.join(__dirname+"/../../resources/public_html", ".")));
server.use(express.urlencoded({ extended: false, limit: "1mb"}));
server.use(express.json());
server.set("view engine", "ejs");
server.set('views', path.join(__dirname, "/views"));

server.get('/', function (req, res){
    res.render("index");
})

server.listen(portnumber, function(){
    console.log(`listening at port: ${portnumber}`)
});