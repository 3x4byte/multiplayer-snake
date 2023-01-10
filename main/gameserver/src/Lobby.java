import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Lobby {

    @Expose
    public final String ID;
    @Expose
    public int lobbySize = 9;
    @Expose (deserialize = false)
    public Player owner; //  can be used by client but does not have to

    @Expose(deserialize = false)
    public Map<Integer, Player> members = new ConcurrentHashMap<>(); //requires manual sync

    @Expose(serialize = false, deserialize = false)
    transient final Object membersRWLock = new Object();
    @Expose(serialize = false, deserialize = false)
    transient Game game;

    Lobby(String id){
        this.ID = id;
    }

    public void startGame(){
        this.game = new Game(members);
    }

    public boolean join(Player player){
        synchronized (membersRWLock){
            if (lobbySize - members.size() > 0){
                members.put(player.id, player);
                player.subscribedToLobbyId = ID;
                return true;
            }
        }
        return false;
    }

    public boolean leave(Player player){
        synchronized (membersRWLock){
            player.subscribedToLobbyId = null;
            return members.remove(player.id) != null;
        }
    }

}
