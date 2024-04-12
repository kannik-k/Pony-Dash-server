package ee.taltech.game.server.packets;

public class OnStartGame {
    private int starterId;
    private int gameId;

    public int getStarterId() {
        return starterId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setStarterId(int starterId) {
        this.starterId = starterId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
}
