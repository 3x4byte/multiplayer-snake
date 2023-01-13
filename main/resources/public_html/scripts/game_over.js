function play_again(){
    socket.send(new Message(OpCode.PLAY_AGAIN).toJson());
}
