package ee.taltech.game.server;

import ee.taltech.game.server.ai.NPC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

public class GameWorld {
    private int gameId; // Hiljem tuleb kasutusse, kui lobby on valmis
    int[][] collisions;
    private List<NPC> aiBots = new ArrayList<>();

    /**
     * Create game-world.
     * @param gameId id of game
     */
    public GameWorld(int gameId) {
        this.generateAiBots();
        this.readTilemapData();
    }

    public List<NPC> getAiBots() {
        return aiBots;
    }

    public int[][] getCollisions() {
        return collisions;
    }

    /**
     * Read map data and save map array.
     */
    private void readTilemapData() {
        try {
            File file = new File("server/assets/Pony_dash_for_spike_salvation_map.tmx");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // For security
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("objectgroup");
            this.collisions = createMapArray(2950, 80, nodeList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create array of map.
     * @param mapWidth width of map
     * @param mapHeight height of map
     * @param objectGroupNodes object-group elements
     * @return int[][] 2d array of map
     */
    private int[][] createMapArray(int mapWidth, int mapHeight, NodeList objectGroupNodes) {
        int[][] mapArray = new int[mapHeight][mapWidth]; // Initialize map array

        // Iterate over each object group node
        for (int i = 0; i < objectGroupNodes.getLength(); i++) {
            Node objectGroupNode = objectGroupNodes.item(i);
            if (objectGroupNode.getNodeType() == Node.ELEMENT_NODE) {
                Element objectGroupElement = (Element) objectGroupNode;
                if (objectGroupElement.getAttribute("name").equals("Ground")
                        || objectGroupElement.getAttribute("name").equals("Platforms")) { // Check if it's the "Ground" or "Platforms" object group
                    NodeList objectNodes = objectGroupElement.getElementsByTagName("object");
                    // Iterate over each object within the object group
                    mapArray = putCollisionsIntoArray(objectNodes, mapHeight, mapWidth, mapArray);
                }
            }
        }
        return mapArray;
    }

    /**
     * Put the parts with collision into the given array and return it.
     * @param objectNodes objects
     * @param mapHeight height of map
     * @param mapWidth width of map
     * @param mapArray array with all zeros
     * @return int[][] array where collision is marked as 1
     */
    private int[][] putCollisionsIntoArray(NodeList objectNodes, int mapHeight, int mapWidth, int[][] mapArray) {
        for (int j = 0; j < objectNodes.getLength(); j++) {
            Element objectElement = (Element) objectNodes.item(j);
            double x = Double.parseDouble(objectElement.getAttribute("x")) / 16; // Tile-width is 16
            double y = Double.parseDouble(objectElement.getAttribute("y")) / 16; // Tile-height is 16
            double width = Double.parseDouble(objectElement.getAttribute("width")) / 16;
            double height = Double.parseDouble(objectElement.getAttribute("height")) / 16;
            // Mark cells in the map array as occupied for this object
            for (double k = y; k < y + height; k++) {
                for (double l = x; l < x + width; l++) {
                    int roundedK = (int) Math.round(k); // Round k to the nearest integer
                    int roundedL = (int) Math.round(l); // Round l to the nearest integer

                    if (roundedK >= 0 && roundedK < mapHeight && roundedL >= 0 && roundedL < mapWidth) {
                        mapArray[roundedK][roundedL] = 1; // 1 represents collision
                    }
                }
            }

        }
        return mapArray;
    }

    /**
     * Generate all bots.
     * <p>
     *     Bots are put to their initial positions (coordinates are in pixels).
     * </p>
     */
    private void generateAiBots() {
        this.aiBots.add(new NPC((22 * 16),(26 * 16)));
        this.aiBots.add(new NPC((64 * 16), (26 * 16)));
        this.aiBots.add(new NPC((93 * 16),(26 * 16)));
        this.aiBots.add(new NPC((64 * 16), (56 * 16)));
    }

}
