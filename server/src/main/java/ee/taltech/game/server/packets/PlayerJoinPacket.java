package ee.taltech.game.server.packets;

public class PlayerJoinPacket {
    private float x;
    private float y;
    private int id;
    private String userName;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }
}
