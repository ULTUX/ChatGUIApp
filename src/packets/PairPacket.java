package packets;

import utils.ClientData;

import java.io.Serializable;

public class PairPacket extends Packet implements Serializable {
    ClientData toConnect;
    private static final long serialVersionUID = 1L;

    public PairPacket(ClientData parent, ClientData toConnect) {
        super(parent.getHostName(), parent.getName(), parent.getPort());
        setPacketType(PacketType.PAIR_CLIENT);
        this.toConnect = toConnect;
    }

    public ClientData getToConnect() {
        return toConnect;
    }
}
