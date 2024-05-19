package ee.taltech.game.server.ai;

import ee.taltech.game.server.Game;
import ee.taltech.game.server.GameServer;
import ee.taltech.game.server.GameWorld;
import ee.taltech.game.server.Player;
import ee.taltech.game.server.packets.PacketCaptured;
import ee.taltech.game.server.packets.PacketOnNpcMove;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.sqrt;

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
    private GameWorld gameWorld;
    private ArrayList<AStar.Node> path; // Current path
    private LocalDateTime captureStart = LocalDateTime.of(2000, 6, 6, 12, 12, 12); // Old date

    /**
     * Create NPC with coordinates and give it a new id.
     *
     * @param tiledX coordinate
     * @param tiledY coordinate
     */
    public NPC(int tiledX, int tiledY, int[][] collisions, int gameId, GameServer gameServer, GameWorld gameWorld) {
        this.tiledX = tiledX;
        this.tiledY = tiledY;
        this.collisions = collisions;
        this.gameId = gameId;
        this.gameServer = gameServer;
        this.gameWorld = gameWorld;
        this.netId = currentId;
        incrementNextId();
        this.moveThread();
    }

    private void moveThread() {
        AStar aStar = new AStar(collisions);

        // Executor for decision-making (higher frequency)
        ScheduledExecutorService decisionExecutor = Executors.newScheduledThreadPool(1);
        Runnable decisionRunnable = () -> {
            if (gameWorld.getAiBots().isEmpty()) {
                decisionExecutor.shutdown(); // Shut down this method when the game with this npc does not exist anymore
            }
            if ((path == null || path.isEmpty()) && Duration.between(captureStart, LocalDateTime.now()).toMillis() > 10000) {
                Map<Player, List<Integer>> closestPlayer = findClosestPlayer();
                if (!closestPlayer.isEmpty()) {
                    int xCur = closestPlayer.entrySet().iterator().next().getValue().get(0);
                    int yCur = closestPlayer.entrySet().iterator().next().getValue().get(1);
                    path = aStar.findPath(tiledX / 16, tiledY / 16, xCur / 16, yCur / 16);
                }
            }
        };
        decisionExecutor.scheduleAtFixedRate(decisionRunnable, 2000, 1, TimeUnit.MILLISECONDS);

        // Executor for movement updates (lower frequency)
        ScheduledExecutorService movementExecutor = Executors.newScheduledThreadPool(1);
        Runnable movementRunnable = () -> {
            if (gameWorld.getAiBots().isEmpty()) {
                // Also shut down movementExecutor if no bots are present
                movementExecutor.shutdown();
            }
            if (path != null && !path.isEmpty()) {
                PacketOnNpcMove movement = new PacketOnNpcMove();
                movement.setNetId(netId);
                AStar.Node node = path.getFirst();
                path.removeFirst();
                tiledX = node.x * 16;
                tiledY = node.y * 16;
                movement.setTiledX(tiledX);
                movement.setTiledY(tiledY);
                gameServer.sendInfoAboutBotMoving(movement, gameId);
            }
        };
        movementExecutor.scheduleAtFixedRate(movementRunnable, 2000, 50, TimeUnit.MILLISECONDS);
    }

    private Map<Player, List<Integer>> findClosestPlayer() {
        List<Game> allGames = gameServer.getGames();
        Game correctGame = null;
        for (Game game : allGames) {
            if (game.getGameId() == gameId) {
                correctGame = game;
                break;
            }
        }
        if (correctGame != null) {
            Map<Integer, Player> playersMap = correctGame.getPlayers();
            Player closestPlayer = null;
            List<Integer> closestCoordinates = new ArrayList<>();
            double shortestDistance = Double.MAX_VALUE;
            for (Map.Entry<Integer, Player> set : playersMap.entrySet()) {
                int playerX = set.getValue().getTiledX();
                int playerY = set.getValue().getTiledY();
                double distanceToPlayer = Math.sqrt(Math.pow((double) playerX - tiledX, 2) + Math.pow((double) playerY - tiledY, 2));
                if (distanceToPlayer < shortestDistance) {
                    shortestDistance = distanceToPlayer;
                    closestCoordinates = new ArrayList<>(List.of(playerX, playerY));
                    closestPlayer = set.getValue();
                }
            }
            Map<Player, List<Integer>> newMap = new HashMap<>();
            newMap.put(closestPlayer, closestCoordinates);
            if (shortestDistance <= 3 * 16) { // If player is 2 tiles away then capture
                PacketCaptured packetCaptured = new PacketCaptured();
                packetCaptured.setTime(LocalDateTime.now().toString());
                packetCaptured.setPlayerId(closestPlayer.getId()); // Cannot be null because the npc object would be removed from the game and the executor would be stopped if there aren't be any players in the game
                captureStart = LocalDateTime.now();
                gameServer.sendInfoAboutCapture(packetCaptured);
            }
            if (shortestDistance < 25 * 16) { // If player is at max 25 tiles away then follow them
                return newMap;
            }
        }
        return new HashMap<>();
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
