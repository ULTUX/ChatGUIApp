package packets;

import java.io.Serializable;

public enum PacketType implements Serializable {
    HANDSHAKE,
    MESSAGE,
    GET_DATA,
    DISCONNECT,
    OK,
    PAIR_CLIENT;

    private static final long serialVersionUID = 1L;

    }
