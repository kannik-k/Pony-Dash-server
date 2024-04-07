package ee.taltech.game.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import ee.taltech.game.server.exceptions.ConnectionException;
import ee.taltech.game.server.lobby.Lobby;
import ee.taltech.game.server.packets.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameServer {

    private Server server;
    private Map<Integer, Player> players = new HashMap<>();
    private int gameId = 1;
    private final List<Game> games = new ArrayList<>();
    Lobby lobby = new Lobby();

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

                if (object instanceof OnStartGame packet) {
                    System.out.println(packet);
                    Game game = new Game(gameId);
                    packet.setGameId(gameId);
                    for (GameConnection peer : lobby.getPeers()) {
                        peer.setGameId(gameId);
                        peer.sendUDP(packet);
                        game.getPlayers().add(peer);
                    }
                    lobby.clearPeers();
                    games.add(game);
                    gameId++;
                }

                if (object instanceof PlayerJoinPacket joinedPlayer) {
                    GameConnection player = (GameConnection) connection;
                    System.out.println(joinedPlayer);
                    System.out.println(player);
                    player.setUserName(joinedPlayer.getUserName());
                    OnStartGame startPacket = new OnStartGame();
                    player.sendUDP(startPacket);

                    ArrayList<OnLobbyJoin> lobbyPlayers = new ArrayList<>();

                    OnLobbyJoin joinedPeer = new OnLobbyJoin();
                    joinedPeer.setId(player.getID());
                    joinedPeer.setName(player.getUserName());

                    for (GameConnection peer : lobby.getPeers()) {
                        if (peer.getGameId() == 0) {
                            peer.sendTCP(joinedPeer);
                            OnLobbyJoin join2 = new OnLobbyJoin();
                            join2.setId(peer.getID());
                            join2.setName(peer.getUserName());
                            lobbyPlayers.add(join2);
                        }
                    }
                    OnLobbyList list = new OnLobbyList();
                    list.setNetId(player.getID());
                    list.setPeers(lobbyPlayers);

                    player.sendTCP(list);

                    lobby.addPeer(player);
                    System.out.println(lobby.getPeers());
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
                    Player peer = players.get(playerID);
                    peer.setX(packet.getX());
                    peer.setY(packet.getY());
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
