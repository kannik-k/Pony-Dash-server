package ee.taltech.game.server.packets;

import java.util.List;

public class OnLobbyList {
    private int netId;
    private List<OnLobbyJoin> peers;

    public int getNetId() {
        return netId;
    }

    public void setNetId(int netId) {
        this.netId = netId;
    }

    public List<OnLobbyJoin> getPeers() {
        return peers;
    }

    public void setPeers(List<OnLobbyJoin> peers) {
        this.peers = peers;
    }
}
