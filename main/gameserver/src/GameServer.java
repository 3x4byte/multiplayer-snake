import java.util.Optional;

public class GameServer {

    private final WSServer<WSMessage> server;

    public GameServer(){
        server = new WSServer<WSMessage>(new WSMessageHandler(), new WSMessage());
    }

    /**
     * Handles incoming messages and is able to send responses
     */
    static class WSMessageHandler implements WSServer.MessageHandler<WSMessage>{

        @Override
        public Optional<WSMessage> handle(WSMessage message) {
            switch (message.getOpcode()){
                //todo
            }
            return Optional.empty();
        }
    }

    /**
     * Eligible for spawning the GameServer
     */
    public static void main(String[] args) {
        GameServer g = new GameServer();
    }

}
