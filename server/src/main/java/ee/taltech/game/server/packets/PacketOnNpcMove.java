package ee.taltech.game.server.packets;

public class PacketOnNpcMove {
    private int netId;
    private float x; // Box2D world coordinates
    private float y;
    private int tiledX; // Tiled coordinates in pixels
    private int tiledY;

    public int getNetId() {
        return netId;
    }

    public void setNetId(int id) {
        this.netId = id;
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
