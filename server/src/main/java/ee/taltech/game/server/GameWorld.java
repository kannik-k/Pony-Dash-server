package ee.taltech.game.server;

import ee.taltech.game.server.ai.NPC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

public class GameWorld {
    private int gameId;
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

    private void readTilemapData () {
        try {
            File file = new File("Server/data/MapGeneral.tmx");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("layer");

            // First collision layer
            Node node = nodeList.item(6); // Layer 6
            Element eElement = (Element) node;
            String data = eElement.getElementsByTagName("data").item(0).getTextContent();
            String[] columns = data.split("\n");
            int[][] collisions = new int[columns.length][columns[1].split(",").length + 1];
            int y = columns.length - 1;
            int x = 0;
            for (String column : columns) {
                String[] rows = column.split(",");
                x = 0;
                for (String row : rows) {
                    if (!Objects.equals(row, "") && Integer.parseInt(row) > 0) {
                        collisions[y][x] = 1;
                    }
                    x++;
                }
                y--;
            }
            this.collisions = collisions;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Generate all bots.
     */
    private void generateAiBots() {
        this.aiBots.add(new NPC(0,0)); // siin saab sättida hiljem algkoordinaadid paika
        this.aiBots.add(new NPC(1, 0));
    }

}
