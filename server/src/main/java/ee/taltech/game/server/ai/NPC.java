package ee.taltech.game.server.ai;

public class NPC {
    private static int currentId = 0;
    private static void incrementNextId() {
        currentId++;
    }
    private final int netId;
    private float x; // Box2d world coordinate
    private float y;
    private int tiledX; // Tiled coordinate in pixels
    private int tiledY;

    /**
     * Create NPC with coordinates and give it a new id.
     * @param tiledX coordinate
     * @param tiledY coordinate
     */
    public NPC(int tiledX, int tiledY) {
        this.tiledX = tiledX;
        this.tiledY = tiledY;
        this.netId = currentId;
        incrementNextId();
        this.moveThread(); // vb võta välja, sest meil ei tohiks ai kohe liikuma hakata
    }

    private void moveThread() {
        // Will use later
    }

    public int getNetId() {
        return netId;
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
