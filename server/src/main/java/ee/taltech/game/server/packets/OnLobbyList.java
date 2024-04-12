package ee.taltech.game.server.packets;

import java.util.List;

public class OnLobbyList {
    private int netId;
    private boolean isHost;
    private List<OnLobbyJoin> peers;

    public int getNetId() {
        return netId;
    }

    public void setNetId(int netId) {
        this.netId = netId;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public List<OnLobbyJoin> getPeers() {
        return peers;
    }

    public void setPeers(List<OnLobbyJoin> peers) {
        this.peers = peers;
    }
}
