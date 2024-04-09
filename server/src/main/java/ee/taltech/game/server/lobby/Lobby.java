package ee.taltech.game.server.lobby;

import ee.taltech.game.server.packets.PlayerJoinPacket;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private final List<PlayerJoinPacket> peers = new ArrayList<>();
    public List<PlayerJoinPacket> getPeers() {
        return peers;
    }
    public void clearPeers() {
        this.peers.clear();
    }
    public void addPeer(PlayerJoinPacket peer) {
        this.peers.add(peer);
    }
}
