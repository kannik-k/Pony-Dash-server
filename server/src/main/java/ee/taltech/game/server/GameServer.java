package ee.taltech.game.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import ee.taltech.game.server.exceptions.ConnectionException;
import ee.taltech.game.server.packets.PacketPlayerConnect;
import ee.taltech.game.server.packets.PacketSendCoordinates;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
            throw new ConnectionException(e.getMessage());
        }
        server.addListener(new Listener() {
            @Override
            public void received (Connection connection, Object object) {
                if (object instanceof PacketPlayerConnect packet) {
                    for (Map.Entry<Integer, Player> set : players.entrySet()) {
                        PacketPlayerConnect packetPlayerConnect = new PacketPlayerConnect();
                        packetPlayerConnect.setPlayerID(set.getKey());
                        packetPlayerConnect.setPlayerName(set.getValue().getPlayerName());
                        server.sendToTCP(connection.getID(), packetPlayerConnect);
                    }
                    Player newPlayer = new Player(packet.getPlayerName());
                    players.put(connection.getID(), newPlayer); // Add player ID and player name to map
                    ((PacketPlayerConnect) object).setPlayerID(connection.getID());
                    server.sendToAllUDP(object);
                }
                if (object instanceof PacketSendCoordinates packet) {
                    int playerID = connection.getID();
                    Player player = players.get(playerID);
                    player.setX(packet.getX());
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
        new GameServer();
    }
}
