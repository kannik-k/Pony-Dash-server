package ee.taltech.game.server;

public class Player {
    private String playerName;
    private float x = 0; // Box2D world coordinates
    private float y = 0;
    private int gameID;
    private int tiledX = Math.round(x * 100); // PPM = 100, this is in pixels
    private int tiledY = Math.round(y * 100); // In pixels

    public Player() {}

    public Player(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getGameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }
    public int getTiledX() {
        return tiledX;
    }

    public void setTiledX(int tiledX) {
        this.tiledX = tiledX;
    }

    public int getTiledY() {
        return tiledY;
    }

    public void setTiledY(int tiledY) {
        this.tiledY = tiledY;
    }
}
