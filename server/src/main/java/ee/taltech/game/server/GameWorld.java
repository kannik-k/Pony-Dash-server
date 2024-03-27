package ee.taltech.game.server;

import ee.taltech.game.server.ai.NPC;

import java.util.ArrayList;
import java.util.List;

public class GameWorld {
    private List<NPC> aiBots = new ArrayList<>();

    /**
     * Create game-world.
     * @param gameId id of game
     */
    public GameWorld(int gameId) {
        this.generateAiBots();
    }

    /**
     * Generate all bots.
     */
    private void generateAiBots() {
        this.aiBots.add(new NPC(0,0)); // siin saab sättida hiljem algkoordinaadid paika
        this.aiBots.add(new NPC(1, 0));
    }

}
