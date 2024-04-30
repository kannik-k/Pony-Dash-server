package ee.taltech.game.server;

import ee.taltech.game.server.packets.PlayerJoinPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

    // game ID
    private final int gameId;

    private GameWorld gameWorld;

    // List of players currently in the game
    private final List<PlayerJoinPacket> playersList = new ArrayList<>();
    private Map<Integer, Player> players = new HashMap<>();

    public Game(int gameId) {
        this.gameId = gameId;
    }

    public int getGameId() {
        return this.gameId;
    }

    public List<PlayerJoinPacket> getPlayersList() {
        return this.playersList;
    }

    public void setPlayers(Map<Integer, Player> players) {
        this.players = new  HashMap<>(players);
    }

    public Map<Integer, Player> getPlayers() {
        return players;
    }

    public GameWorld getGameWorld() {
        return gameWorld;
    }

    public void setGameWorld(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
    }
}
