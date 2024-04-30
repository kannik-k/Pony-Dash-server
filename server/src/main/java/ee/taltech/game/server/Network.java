package ee.taltech.game.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import ee.taltech.game.server.packets.*;

import java.util.ArrayList;
import java.util.List;

public class Network {

    private Network() {
        // Prevent instantiation
    }

    /**
     * Register endpoint to the network.
     *
     * @param endPoint The endpoint.
     */
    public static void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(Packet.class);
        kryo.register(PacketPlayerConnect.class);
        kryo.register(PacketSendCoordinates.class);
        kryo.register(ArrayList.class);
        kryo.register(List.class);
        kryo.register(ee.taltech.game.server.Player.class);
        kryo.register(int.class);

        kryo.register(PlayerJoinPacket.class);
        kryo.register(OnStartGame.class);
        kryo.register(OnLobbyJoin.class);
        kryo.register(OnLobbyList.class);
        kryo.register(PacketGameId.class);
        kryo.register(PacketGameOver.class);
        kryo.register(PacketSinglePlayer.class);

        kryo.register(PacketOnSpawnNpc.class);
        kryo.register(PacketOnNpcMove.class);
        kryo.register(PacketCaptured.class);
    }
}
