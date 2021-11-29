package packets;

import java.io.Serializable;

public class MessagePacket extends Packet implements Serializable {
    private static final long serialVersionUID = 1L;
    public MessagePacket(String hostName, String name, int port, String message) {
        super(hostName, name, port);
        setPacketType(PacketType.MESSAGE);
        setData(message);
    }
}
