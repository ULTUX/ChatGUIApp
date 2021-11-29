package packets;

import java.io.Serializable;

public class SuccessPacket extends Packet implements Serializable {
    private static final long serialVersionUID = 1L;
    boolean isOK;

    public SuccessPacket(String hostName, String name, int port, boolean isOK) {
        super(hostName, name, port);
        this.isOK = isOK;
        setPacketType(PacketType.OK);
    }
    public SuccessPacket(String hostName, String name, int port, boolean isOK, String additionalData) {
        this(hostName, name, port, isOK);
        setData(additionalData);

    }
}
