package ee.taltech.game.server.packets;

import ee.taltech.game.server.Player;

import java.time.LocalDateTime;

public class PacketCaptured {
    private LocalDateTime time;
    private int playerId;
    private Player player;

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
