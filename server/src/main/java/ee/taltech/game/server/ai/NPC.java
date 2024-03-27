package ee.taltech.game.server.ai;

public class NPC {
    private static int currentId = 0;
    private static void incrementNextId() {
        currentId++;
    }
    private final int netId;
    private float x;
    private float y;

    /**
     * Create NPC with coordinates and give it a new id.
     * @param x coordinate
     * @param y coordinate
     */
    public NPC(float x, float y) {
        this.x = x;
        this.y = y;
        this.netId = currentId;
        incrementNextId();
        this.moveThread(); // vb võta välja, sest meil ei tohiks ai kohe liikuma hakata
    }

    private void moveThread() {

    }
}
