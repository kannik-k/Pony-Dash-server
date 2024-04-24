package ee.taltech.game.server.ai;

import ee.taltech.game.server.Game;
import ee.taltech.game.server.GameServer;
import ee.taltech.game.server.Network;
import ee.taltech.game.server.packets.PacketOnNpcMove;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private final int[][] collisions;
    private int gameId;
    private GameServer gameServer;
    private ArrayList<AStar.Node> path; // Current path

    /**
     * Create NPC with coordinates and give it a new id.
     * @param tiledX coordinate
     * @param tiledY coordinate
     */
    public NPC(int tiledX, int tiledY, int[][] collisions, int gameId, GameServer gameServer) {
        this.tiledX = tiledX;
        this.tiledY = tiledY;
        this.collisions = collisions;
        this.gameId = gameId;
        this.gameServer = gameServer;
        this.netId = currentId;
        incrementNextId();
        this.moveThread();
    }

    private void moveThread() {
        AStar aStar = new AStar(collisions);
        Runnable botRunnable = () -> { // Ajutine suvaline liikumine
            if (path == null || path.isEmpty()) {
                Random rand = new Random();
                boolean foundGoodLocation = false;
                while (!foundGoodLocation) {
                    int xCur = rand.nextInt(2950 + 1);
                    int yCur = rand.nextInt(80 + 1);
                    if (collisions[yCur][xCur] == 0) {
                        foundGoodLocation = true;
                        path = aStar.findPath(tiledX / 16, tiledY / 16, xCur, yCur);
                        System.out.println(path);
                    }
                }
            } else {
                PacketOnNpcMove movement = new PacketOnNpcMove();
                movement.setNetId(netId);
                AStar.Node node = path.getFirst();
                path.removeFirst();
                tiledX = node.x * 16;
                tiledY = node.y * 16;
                System.out.println("x :" + node.x + "y: " + node.y);
                movement.setTiledX(tiledX);
                movement.setTiledY(tiledY);
                gameServer.sendInfoAboutBotMoving(movement, gameId);
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(botRunnable, 2000, 150, TimeUnit.MILLISECONDS); // Runs botRunnable every 300 milliseconds
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
