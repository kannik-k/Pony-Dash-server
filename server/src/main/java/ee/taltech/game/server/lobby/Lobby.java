package ee.taltech.game.server.lobby;

import ee.taltech.game.server.packets.GameConnection;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private final List<GameConnection> peers = new ArrayList<>();
    public List<GameConnection> getPeers() {
        return peers;
    }
    public void clearPeers() {
        this.peers.clear();
    }
    public void addPeer(GameConnection peer) {
        this.peers.add(peer);
    }
}
