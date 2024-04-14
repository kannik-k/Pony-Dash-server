package ee.taltech.game.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import ee.taltech.game.server.ai.NPC;
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
     *
     * Creates a new server, registers it to the network and connects it to a port.
     * A listener is added for incoming packets.
     *
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
        GameWorld gameWorld = new GameWorld(1); // GameId tuleb siin hiljem ära muuta, hiljem tuleb kasutusele

        server.addListener(new Listener() {
            /**
             * Create listener for incoming packets.
             * <p>
             *     There are four kinds of packets the server receives.
             *     1. OnStartGame
             *     2. PlayerJoinPacket
             *     3. The packet PacketPlayerConnect is received when a player joins the game. All of the connected
             *     players and npc-s existence information is sent to the new connected player. The new connected player is
             *     created, added to the players map and its existence information is sent to all other connected
             *     players.
             *     4. The packet PacketSendCoordinates is received constantly with updated players' location. The
             *     coordinates of a player are then sent to all other players that are connected.
             * </p>
             * @param connection
             * @param object
             */
            @Override
            public void received (Connection connection, Object object) {
                int gameID = -1;
                if (object instanceof OnStartGame) {
                    gameID = ((OnStartGame) object).getGameId();
                } else if (object instanceof PlayerJoinPacket) {
                    gameID = ((PlayerJoinPacket) object).getGameId();
                }

                if (gameID == 0) {

                    if (object instanceof OnStartGame packet) {
                        Game game = new Game(gameId);
                        packet.setGameId(gameId);
                        for (PlayerJoinPacket peer : lobby.getPeers()) {
                            peer.setGameId(gameId);
                            server.sendToTCP(peer.getId(), packet);
                            game.getPlayers().add(peer);
                        }
                        for (PlayerJoinPacket player : lobby.getPeers()) {
                            System.out.println("Player game id:" + player.getGameId());
                            game.getPlayers().add(player);
                        }
                        lobby.clearPeers();
                        games.add(game);
                        gameId++;
                    }

                    if (object instanceof PlayerJoinPacket joinedPlayer) {
                        joinedPlayer.setUserName(joinedPlayer.getUserName());

                        ArrayList<OnLobbyJoin> lobbyPlayers = new ArrayList<>();

                        OnLobbyJoin joinedPeer = new OnLobbyJoin();
                        joinedPeer.setId(connection.getID());
                        joinedPeer.setName(joinedPlayer.getUserName());
                        for (PlayerJoinPacket peer : lobby.getPeers()) {
                            if (peer.getGameId() == 0) {
                                server.sendToTCP(connection.getID(), joinedPeer);
                                OnLobbyJoin join2 = new OnLobbyJoin();
                                join2.setId(connection.getID());
                                join2.setName(peer.getUserName());
                                lobbyPlayers.add(join2);
                            }
                        }
                        OnLobbyList list = new OnLobbyList();
                        list.setNetId(joinedPlayer.getId());
                        list.setPeers(lobbyPlayers);

                        // Send game id to player
                        PacketGameId packetGameId = new PacketGameId();
                        packetGameId.setGameId(gameId);
                        System.out.println("Game id for sending: " + packetGameId.getGameId());
                        server.sendToTCP(connection.getID(), packetGameId);

                        server.sendToTCP(connection.getID(), list);
                        joinedPeer.setId(connection.getID());
                        lobby.addPeer(joinedPlayer, connection.getID());
                    }
                }
                // System.out.println("Game id was -1");
                System.out.println("Games:" + games);

                int receivedGameId = -1;

                if (object instanceof PacketSendCoordinates) {
                    receivedGameId = ((PacketSendCoordinates) object).getGameID();
                } else if (object instanceof PacketPlayerConnect) {
                    receivedGameId = ((PacketPlayerConnect) object).getGameID();
                }

                Game currentGame = null;
                for (Game game : games) {
                    System.out.println("in the loop " + game.getGameId());
                    if (game.getGameId() == receivedGameId) {
                        currentGame = game;
                        System.out.println("I got the current game:" + currentGame);
                        break;
                    }
                }

                if (object instanceof PacketPlayerConnect packet) {
                    System.out.println("i recived PacketPlayerConnect with game id: " + packet.getGameID());
                    for (Map.Entry<Integer, Player> set : players.entrySet()) {
                        PacketPlayerConnect packetPlayerConnect = new PacketPlayerConnect();
                        packetPlayerConnect.setPlayerID(set.getKey());
                        packetPlayerConnect.setPlayerName(set.getValue().getPlayerName());
                        server.sendToTCP(connection.getID(), packetPlayerConnect);
                    }
                    for (NPC npc: gameWorld.getAiBots()) {
                        PacketOnSpawnNpc packetOnSpawnNpc = new PacketOnSpawnNpc();
                        packetOnSpawnNpc.setId(npc.getNetId());
                        packetOnSpawnNpc.setTiledX(npc.getTiledX());
                        packetOnSpawnNpc.setTiledY(npc.getTiledY());
                        server.sendToTCP(connection.getID(), packetOnSpawnNpc);
                    }
                    Player newPlayer = new Player(packet.getPlayerName());
                    players.put(connection.getID(), newPlayer); // Add player ID and player name to map
                    ((PacketPlayerConnect) object).setPlayerID(connection.getID());
                    server.sendToAllUDP(object);
                }

                if (object instanceof PacketSendCoordinates packet) {
                    System.out.println("i recived PacketSendCoordinates with game id: " + packet.getGameID());
                    int playerID = connection.getID();
                    Player peer = players.get(playerID);

                    peer.setX(packet.getX());
                    peer.setY(packet.getY());
                    peer.setTiledX(packet.getTiledX());
                    peer.setTiledY(packet.getTiledY());
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
