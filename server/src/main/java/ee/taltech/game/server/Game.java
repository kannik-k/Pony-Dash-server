package ee.taltech.game.server;

import ee.taltech.game.server.packets.PlayerJoinPacket;

import java.util.ArrayList;
import java.util.List;

public class Game {

     // game ID
    private final int gameId;

     // List of players currently in the game
    private final List<PlayerJoinPacket> players = new ArrayList<>();

    public Game(int gameId) {
        this.gameId = gameId;
    }
    public int getGameId() {
        return this.gameId;
    }
    public List<PlayerJoinPacket> getPlayers() {
        return this.players;
    }
}
