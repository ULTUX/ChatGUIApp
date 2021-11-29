package packets;

import exceptions.UnknownPacketException;

import java.io.Serializable;

public class HandshakePacket extends Packet implements Serializable {
    private static final long serialVersionUID = 1L;
    public HandshakePacket(String hostName, String name, int port) throws UnknownPacketException {
        super(hostName, name, port);
        setPacketType(PacketType.HANDSHAKE);
    }
}
