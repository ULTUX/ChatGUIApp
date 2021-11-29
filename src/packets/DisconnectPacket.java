package packets;

import utils.ClientData;

import java.io.Serializable;

public class DisconnectPacket extends Packet implements Serializable {
    ClientData toDisconnect;
    private static final long serialVersionUID = 1L;

    public DisconnectPacket(ClientData toDisconnect) {
        super(toDisconnect.getHostName(), toDisconnect.getName(), toDisconnect.getPort());
        this.toDisconnect = toDisconnect;
        setPacketType(PacketType.DISCONNECT);
    }

    public ClientData getToDisconnect() {
        return toDisconnect;
    }
}
