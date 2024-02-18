package ee.taltech.game.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import ee.taltech.game.server.packets.PacketPlayerConnect;
import ee.taltech.game.server.packets.PacketSendCoordinates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameServer {

    private Server server;
    private Map<Integer, Player> players = new HashMap<>();

    public GameServer() {
        server = new Server();
        server.start();
        Network.register(server);
        try {
            server.bind(8080, 8081);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof PacketPlayerConnect) {
                    for (Map.Entry<Integer, Player> set : players.entrySet()) {
                        PacketPlayerConnect packetPlayerConnect = new PacketPlayerConnect();
                        packetPlayerConnect.setPlayerID(set.getKey());
                        packetPlayerConnect.setPlayerName(set.getValue().getPlayerName());
                        server.sendToTCP(connection.getID(), packetPlayerConnect);
                    }
                    Player newPlayer = new Player(((PacketPlayerConnect) object).getPlayerName());
                    players.put(connection.getID(), newPlayer); // Add player ID and player name to map
                    ((PacketPlayerConnect) object).setPlayerID(connection.getID());
                    server.sendToAllUDP(object);
                    System.out.println(players);
                }
                if (object instanceof PacketSendCoordinates) {
                    int playerID = connection.getID();
                    Player player = players.get(playerID);
                    player.setX(((PacketSendCoordinates) object).getX());
                    player.setY(((PacketSendCoordinates) object).getY());
                    server.sendToAllUDP(object);
                }
            }
            @Override
            public void disconnected(Connection connection) {
                players.remove(connection.getID());
            }
        });
    }

    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
    }
}
