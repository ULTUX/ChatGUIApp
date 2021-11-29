package packets;

import exceptions.UnknownPacketException;

import java.io.Serializable;

public class Packet implements Serializable {
    private static final long serialVersionUID = 1L;

    private String[] packetData;
    private int packetLength;
    private PacketType packetType;
    private String hostName;
    private int port;
    private String data;
    private String name;

    public Packet(String data) throws UnknownPacketException {
        this.packetData = data.split("#");
        packetLength = packetData.length;
        if (packetData.length < 4) throw new UnknownPacketException("Packet is too small");
        this.hostName = packetData[1];
        this.port = Integer.parseInt(packetData[2]);
        name = packetData[3];
        this.data = packetData[4];
    }
    public Packet(){
    }

    public Packet(String hostName, String name, int port) {
        this.hostName = hostName;
        this.name = name;
        this.port = port;

    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getPacketData() {
        return packetData;
    }

    public int getPacketLength() {
        return packetLength;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public String getData() {
        return data;
    }

    public void setPacketType(PacketType packetType) {
        this.packetType = packetType;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return packetType.toString()+"#"+hostName+"#"+port+"#"+name+"#"+data;
    }
}
