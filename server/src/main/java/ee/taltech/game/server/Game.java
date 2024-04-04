package ee.taltech.game.server;

import ee.taltech.game.server.packets.GameConnection;

import java.util.ArrayList;
import java.util.List;

public class Game {

     // game ID
    private final int gameId;

     // List of players currently in the game
    private final List<GameConnection> players = new ArrayList<>();

    public Game(int gameId) {
        this.gameId = gameId;
    }
    public int getGameId() {
        return this.gameId;
    }
    public List<GameConnection> getPlayers() {
        return this.players;
    }
}