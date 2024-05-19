package ee.taltech.game.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import ee.taltech.game.server.ai.NPC;
import ee.taltech.game.server.exceptions.ConnectionException;
import ee.taltech.game.server.lobby.Lobby;
import ee.taltech.game.server.packets.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class GameServer {

    private Server server;
    private Map<Integer, Player> players = new HashMap<>();
    private Map<Integer, Player> singlePlayers = new HashMap<>();
    private int gameId = 1;
    private List<Game> games = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
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
        GameServer gameServer = this; // For creating GameWorld objects

        server.addListener(new Listener() {
            /**
             * Create listener for incoming packets.
             * <p>
             *     There are ten kinds of packets the server receives.
             *     1. PacketPlayerLeftLobby is received when someone leaves the lobby.
             *     2. OnStartGame is received when a multiplayer game is started.
             *     3. The PacketSinglePlayer and OnStartGame packets are received when players connect the game. All of the connected
             *     players and npc-s existence information is sent to the new connected player. The new connected player is
             *     created, added to the players map and its existence information is sent to all other connected
             *     players.
             *     4. PlayerJoinPacket is sent to server when player joins with the lobby. If there are already players
             *     in the lobby, OnLobbyJoin and OnLobbyList will notify other players in the lobby and the new player
             *     will receive a list of players already in the lobby. New player will receive a gameId.
             *     5. PacketUpdateLobby
             *     6. The packet PacketSendCoordinates is received constantly with updated players' location. The
             *     coordinates of a player are then sent to all other players that are connected.
             *     7. PacketPlayerConnect is received when a new client is created.
             *     8. PacketGameOver contains winners id and name. When the packet reaches server, it will sent it to
             *     every other player in the game.
             *     9. PacketPowerUpTaken is received when someone picks up a power-up, the info is then sent to other
             *     players in that game.
             *     10. PacketPlayerExitedGame is received when someone disconnects during a game. The info is then sent
             *     to other players in that game.
             * </p>
             * @param connection
             * @param object
             */
            @Override
            public void received (Connection connection, Object object) {

                if (object instanceof PacketPlayerLeftLobby packet) {
                    System.out.println("received packet player left lobby.");
                    List<PlayerJoinPacket> peers = lobby.getPeers();
                    String nameToRemove = packet.getName();
                    int playerIdToRemove = packet.getId();
                    for (int i = 0; i < peers.size(); i++) {
                        PlayerJoinPacket player = peers.get(i);
                        System.out.println("player id: " + player.getId());
                        System.out.println("Remove id: " + playerIdToRemove);
                        System.out.println("Name: " + player.getUserName());
                        System.out.println("Name to remove: " + nameToRemove);
                        if (Objects.equals(player.getUserName(), nameToRemove)) {
                            peers.remove(i);
                        }
                    }

                    PacketUpdateLobby update = new PacketUpdateLobby();
                    update.setLobbySize(lobby.getPeers().size());

                    for (PlayerJoinPacket player : lobby.getPeers()) {
                        System.out.println("player count: " + lobby.getPeers().size());
                        server.sendToTCP(player.getId(), update);
                    }
                }

                int gameID = -1;
                if (object instanceof OnStartGame) {
                    gameID = ((OnStartGame) object).getGameId();
                } else if (object instanceof PlayerJoinPacket) {
                    gameID = ((PlayerJoinPacket) object).getGameId();
                } else if (object instanceof PacketSinglePlayer) {
                    gameID = ((PacketSinglePlayer) object).getGameId();
                } else if (object instanceof PacketUpdateLobby) {
                    gameID = ((PacketUpdateLobby) object).getGameId();
                }

                if (gameID == 0) {

                    if (object instanceof PacketSinglePlayer singlePlayer) {
                        Game game = new Game(gameId);
                        singlePlayer.setGameId(gameId);
                        singlePlayer.setId(connection.getID());

                        // Great a new player
                        Player newPlayer = new Player(singlePlayer.getUserName());
                        newPlayer.setGameID(gameId); // Set game id for player
                        newPlayer.setId(connection.getID()); // Set connection id for player
                        singlePlayers.put(connection.getID(), newPlayer); // Add player ID and player name to map

                        PacketPlayerConnect packetPlayerConnect = new PacketPlayerConnect();
                        packetPlayerConnect.setPlayerID(singlePlayer.getId());
                        packetPlayerConnect.setPlayerName(singlePlayer.getUserName());
                        packetPlayerConnect.setGameID(singlePlayer.getGameId());
                        server.sendToTCP(singlePlayer.getId(), packetPlayerConnect);

                        game.setPlayers(singlePlayers);
                        GameWorld gameWorld = new GameWorld(gameId, game, gameServer);

                        for (NPC npc : gameWorld.getAiBots()) {
                            PacketOnSpawnNpc packetOnSpawnNpc = new PacketOnSpawnNpc();
                            packetOnSpawnNpc.setId(npc.getNetId());
                            packetOnSpawnNpc.setTiledX(npc.getTiledX());
                            packetOnSpawnNpc.setTiledY(npc.getTiledY());
                            server.sendToTCP(singlePlayer.getId(), packetOnSpawnNpc);
                        }

                        game.setGameWorld(gameWorld);

                        games.add(game);
                        gameId++;
                        singlePlayers.clear();
                    }

                    if (object instanceof OnStartGame packet) {
                        Game game = new Game(gameId);
                        packet.setGameId(gameId);
                        for (PlayerJoinPacket peer : lobby.getPeers()) {
                            for (Map.Entry<Integer, Player> set : players.entrySet()) {
                                set.getValue().setGameID(gameId); // Set game id for player
                                set.getValue().setId(set.getKey()); // Set player id
                                PacketPlayerConnect packetPlayerConnect = new PacketPlayerConnect();
                                packetPlayerConnect.setPlayerID(set.getKey());
                                packetPlayerConnect.setPlayerName(set.getValue().getPlayerName());
                                server.sendToTCP(peer.getId(), packetPlayerConnect);
                            }

                            peer.setGameId(gameId);
                            server.sendToTCP(peer.getId(), packet);
                            game.getPlayersList().add(peer);
                        }
                        for (PlayerJoinPacket player : lobby.getPeers()) {
                            game.getPlayersList().add(player);
                        }

                        game.setPlayers(players);
                        GameWorld gameWorld = new GameWorld(gameId, game, gameServer);

                        for (PlayerJoinPacket peer : lobby.getPeers()) {
                            for (NPC npc : gameWorld.getAiBots()) {
                                PacketOnSpawnNpc packetOnSpawnNpc = new PacketOnSpawnNpc();
                                packetOnSpawnNpc.setId(npc.getNetId());
                                packetOnSpawnNpc.setTiledX(npc.getTiledX());
                                packetOnSpawnNpc.setTiledY(npc.getTiledY());
                                server.sendToTCP(peer.getId(), packetOnSpawnNpc);
                            }
                        }

                        game.setGameWorld(gameWorld);

                        lobby.clearPeers();
                        games.add(game);
                        gameId++;
                        players.clear();
                    }

                    if (object instanceof PlayerJoinPacket joinedPlayer) {
                        PacketUpdateLobby update = new PacketUpdateLobby();

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

                        // Great a new player
                        Player newPlayer = new Player(joinedPlayer.getUserName());
                        players.put(connection.getID(), newPlayer); // Add player ID and player name to map

                        // Send game id to player
                        PacketGameId packetGameId = new PacketGameId();
                        packetGameId.setGameId(gameId);
                        server.sendToTCP(connection.getID(), packetGameId);

                        server.sendToTCP(connection.getID(), list);
                        joinedPeer.setId(connection.getID());
                        lobby.addPeer(joinedPlayer, connection.getID());
                        update.setLobbySize(lobby.getPeers().size());

                        for (PlayerJoinPacket player : lobby.getPeers()) {
                            server.sendToTCP(player.getId(), update);
                        }
                    }
                }

                int receivedGameId = -1;

                if (object instanceof PacketSendCoordinates) {
                    receivedGameId = ((PacketSendCoordinates) object).getGameID();
                } else if (object instanceof PacketPlayerConnect) {
                    receivedGameId = ((PacketPlayerConnect) object).getGameID();
                } else if (object instanceof PacketGameOver) {
                    receivedGameId = ((PacketGameOver) object).getGameId();
                } else if (object instanceof PacketPowerUpTaken) {
                    receivedGameId = ((PacketPowerUpTaken) object).getGameId();
                } else if (object instanceof PacketPlayerExitedGame) {
                    receivedGameId = ((PacketPlayerExitedGame) object).getGameId();
                }

                Game currentGame = null;
                for (Game game : games) {
                    if (game.getGameId() == receivedGameId) {
                        currentGame = game;
                        break;
                    }
                }
                if (currentGame != null) {

                    if (object instanceof PacketSendCoordinates packet) {
                        int playerID = packet.getPlayerID();
                        if (currentGame.getPlayers().containsKey(playerID)) {
                            Player peer = currentGame.getPlayers().get(playerID);

                            peer.setX(packet.getX());
                            peer.setY(packet.getY());
                            peer.setTiledX(packet.getTiledX());
                            peer.setTiledY(packet.getTiledY());
                            server.sendToAllUDP(object);
                        }
                    }

                    if (object instanceof  PacketPowerUpTaken packet) {
                        for (Map.Entry<Integer, Player> set : currentGame.getPlayers().entrySet()) {
                            server.sendToTCP(set.getKey(), packet);
                        }
                    }

                    if (object instanceof PacketPlayerExitedGame packet) {
                        currentGame.removePlayer(packet.getId());
                        for (Map.Entry<Integer, Player> set : currentGame.getPlayers().entrySet()) {
                            if (set.getKey() != packet.getId()) {
                                server.sendToTCP(set.getKey(), packet);
                            }
                        }
                    }

                    if (object instanceof PacketGameOver packet) {
                        for (Map.Entry<Integer, Player> set : currentGame.getPlayers().entrySet()) {
                            server.sendToTCP(set.getKey(), packet);
                            games.remove(currentGame);
                        }
                    }
                }
            }

            /**
             * Remove disconnected player. Remove game and bots if nobody is in that game anymore.
             *
             * @param connection (TCP or UDP)
             */
            @Override
            public void disconnected(Connection connection) {
                players.remove(connection.getID());
                for (Game game: games) {
                    if (game.getPlayers().containsKey(connection.getID())) {
                        game.getPlayers().remove(connection.getID());
                        if (game.getPlayers().isEmpty()) {
                            game.getGameWorld().deleteBots();
                            games.remove(game);
                            break;
                        }
                    }
                }
            }
        });
    }

    /**
     * Send info about AI characters movement to the players in the corresponding game.
     * @param movement PacketOnNpcMovement object
     * @param gameId id of the game in which the bot moved
     */
    public void sendInfoAboutBotMoving(PacketOnNpcMove movement, int gameId) {
        lock.lock();
        try {
            for (Game game: this.games) {
                if (game.getGameId() == gameId) {
                    for (Map.Entry<Integer, Player> set : game.getPlayers().entrySet()) {
                        if (set.getValue() != null && set.getValue().getGameID() == gameId) {
                            server.sendToTCP(set.getKey(), movement);
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Send player info about them being captured by bot.
     * @param packetCaptured packet with start time of capture
     */
    public void sendInfoAboutCapture(PacketCaptured packetCaptured) {
        lock.lock();
        try {
            server.sendToTCP(packetCaptured.getPlayerId(), packetCaptured);
        } finally {
            lock.unlock();
        }
    }

    public List<Game> getGames() {
        return games;
    }

    public static void main(String[] args) {
        new GameServer();
    }
}
