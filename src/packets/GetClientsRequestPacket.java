package packets;

import java.io.Serializable;

public class GetClientsRequestPacket extends Packet implements Serializable {
    private static final long serialVersionUID = 1L;

    public GetClientsRequestPacket(String hostName, String name, int port) {
        super(hostName, name, port);
        setPacketType(PacketType.GET_DATA);
    }
}
