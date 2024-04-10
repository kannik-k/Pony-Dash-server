package ee.taltech.game.server;

import ee.taltech.game.server.ai.NPC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.File;
import java.util.Objects;
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

    public int[][] getCollisions() {
        return collisions;
    }

    /*
    private void readTilemapData () {
        try {
            File file = new File("Server/data/Pony_dash_for_spike_salvation_map.tmx");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("layer");

            // First collision layer
            int[][] collisionsHere1 = (int[][]) getLayerCollisionsArrayAndColumns(nodeList, 6).get(0);

            System.out.println(Arrays.deepToString(collisionsHere1));
            // Second collision layer
            int[][] collisionsHere2 = (int[][]) getLayerCollisionsArrayAndColumns(nodeList, 7).get(0);

            // Combine the arrays
            String[] columns = (String[]) getLayerCollisionsArrayAndColumns(nodeList, 6).get(1);
            int[][] combinedCollisions = new int[columns.length][columns[1].split(",").length + 1];

            for (int i = 0; i < columns.length; i++) {
                for (int j = 0; j < columns[1].split(",").length + 1; j++) {
                    combinedCollisions[i][j] = collisionsHere1[i][j] | collisionsHere2[i][j];
                }
            }
            this.collisions = combinedCollisions;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private List<Serializable[]> getLayerCollisionsArrayAndColumns(NodeList nodeList, int layerIndex) {
        Node node = nodeList.item(layerIndex);
        Element eElement = (Element) node;
        String data = eElement.getElementsByTagName("data").item(0).getTextContent();
        String[] columns = data.split("\n");
        int[][] collisionsHere = new int[columns.length][columns[1].split(",").length + 1];
        int y = columns.length - 1;
        int x = 0;
        for (String column : columns) {
            String[] rows = column.split(",");
            x = 0;
            for (String row : rows) {
                if (!Objects.equals(row, "") && Integer.parseInt(row) > 0) {
                    collisionsHere[y][x] = 1;
                }
                x++;
            }
            y--;
        }
        return List.of(collisionsHere, columns);
    }
     */

    private void readTilemapData() {
        try {
            File file = new File("Server/data/Pony_dash_for_spike_salvation_map.tmx");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("objectgroup");
            this.collisions = createMapArray(2950, 80, nodeList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                                    mapArray[roundedK][roundedL] = 1; // Or any other value representing collision
                                }
                            }
                        }

                    }
                }
            }
        }

        return mapArray;
    }

    /**
     * Generate all bots.
     */
    private void generateAiBots() {
        this.aiBots.add(new NPC(0,0)); // siin saab sättida hiljem algkoordinaadid paika
        this.aiBots.add(new NPC(1, 0));
    }

}
