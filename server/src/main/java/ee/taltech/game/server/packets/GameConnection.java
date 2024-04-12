package ee.taltech.game.server.packets;

import com.esotericsoftware.kryonet.Connection;

public class GameConnection extends Connection {
    private String userName;
    private float x;
    private float y;
    private int gameId;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getGameId() {
        return gameId;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
}
