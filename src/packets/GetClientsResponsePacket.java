package packets;

import exceptions.UnknownPacketException;
import utils.ClientData;

import java.io.Serializable;
import java.util.List;

public class GetClientsResponsePacket extends Packet implements Serializable {
    private static final long serialVersionUID = 1L;
    List<ClientData> clientData;
    public GetClientsResponsePacket(List<ClientData> data) {
        super();
        this.clientData = data;

    }

    public List<ClientData> getReceivedClients(){
        return clientData;
    }
}
