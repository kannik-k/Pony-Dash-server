package ee.taltech.game.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import ee.taltech.game.server.exceptions.ConnectionException;
import ee.taltech.game.server.packets.PacketLobby;
import ee.taltech.game.server.packets.PacketPlayerConnect;
import ee.taltech.game.server.packets.PacketSendCoordinates;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameServer {

    private Server server;
    private Map<Integer, Player> players = new HashMap<>();
    private Map<Integer, List<String>> lobbies = new HashMap<>();

    /**
     * Create game-server.
     * <p>
     *     Creates a new server, registers it to the network and connects it to a port.
     *     A listener is added for incoming packets.
     * </p>
     */
    public GameServer() {
        server = new Server();
        server.start();
        Network.register(server);
        try {
            server.bind(8080, 8081);
        } catch (IOException e) {
            throw new ConnectionException(e.getMessage());
        }
        GameWorld gameWorld = new GameWorld(1); // GameId tuleb siin hiljem ära muuta
        System.out.println(Arrays.deepToString(gameWorld.getCollisions()));

        server.addListener(new Listener() {
            /**
             * Create listener for incoming packets.
             * <p>
             *     There are two kinds of packets the server receives.
             *     1. The packet PacketPlayerConnect is received when a player joins the game. All of the connected
             *     players existence information is sent to the new connected player. The new connected player is
             *     created, added to the players map and its existence information is sent to all other connected
             *     players.
             *     2. The packet PacketSendCoordinates is received constantly with updated players' location. The
             *     coordinates of a player are then sent to all other players that are connected.
             * </p>
             * @param connection
             * @param object
             */
            @Override
            public void received (Connection connection, Object object) {
                if (object instanceof PacketLobby packet) {
                    int lobbyID = packet.getLobbyID();
                    List<String> lobbyPlayers = packet.getPlayers();
                    if (lobbies.containsKey(lobbyID)) {
                        List<String> existingPlayers = lobbies.get(lobbyID);
                        existingPlayers.addAll(lobbyPlayers);
                    } else {
                        lobbies.put(lobbyID, lobbyPlayers);
                    }
                }
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
                    player.setY(packet.getY());
                    server.sendToAllUDP(object);
                }
            }

            /**
             * Remove disconnected player from players map.
             *
             * @param connection (TCP or UDP)
             */
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
