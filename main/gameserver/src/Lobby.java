import java.util.HashMap;


public class Lobby {
    public final String ID;
    public String name;
    private int maxLobbySize = 9;
    HashMap<Integer, Player> members = new HashMap<>(); //requires manual sync
    final Object membersRWLock = new Object();
    Game game;

    Lobby(String id){
        this.ID = id;
    }

    public void startGame(){
        System.out.println("MAKING GAME FROM: " + members + " with length " + members.size());
        this.game = new Game(members);
    }

    public boolean join(Player player){
        synchronized (membersRWLock){
            if (maxLobbySize - members.size() > 0){
                members.put(player.id, player);
                return true;
            }
        }
        return false;
    }

    public boolean leave(Player player){
        synchronized (membersRWLock){
            return members.remove(player.id) != null;
        }
    }

}
