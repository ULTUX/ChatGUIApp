package client;

import exceptions.ClientNotConnectedException;
import exceptions.UnknownPacketException;
import packets.*;
import utils.ClientData;
import utils.ConnectionStatus;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class PacketManager implements Runnable {

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Client client;
    private String host;
    private int port;
    private Socket socket;
    private ConnectionStatus status;
    private ChatWindow chatHandler;
    private boolean exit = false;
    private String name;
    public PacketManager(Client client, Socket socket, ChatWindow chatHandler, ClientData pair) throws IOException, UnknownPacketException {
        this.socket = socket;
        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        this.client = client;
        status = ConnectionStatus.NOT_GREET;
        this.chatHandler = chatHandler;
        chatHandler.setCurrentPacketManager(this);
        new Thread(this).start();
    }

    public void sendMessage(String message) throws ClientNotConnectedException, IOException {
        if (!status.equals(ConnectionStatus.CONNECTED)) throw new ClientNotConnectedException("The client is not connected to that host, could not send a message.");
        MessagePacket packet = new MessagePacket(client.getHostName(), client.name, client.getPORT(), message);
        sendPacket(packet);
    }

    private void readMessage(MessagePacket packet) {
        System.out.println("RANDOM PACKET RECEIVED BUT ITS MESSAGE");
            if (packet.getData() != null) {
                System.out.println(packet.getData());
                chatHandler.messageReceived(packet.getData());
            }
    }

    private void readHandshake(){
        try {
            Packet handshakePacket = (Packet) objectInputStream.readObject();
            if (handshakePacket.getPacketType().equals(PacketType.HANDSHAKE)){
                HandshakePacket packet = (HandshakePacket) handshakePacket;
                this.name = packet.getName();
                this.host = packet.getHostName();
                this.port = packet.getPort();
                System.out.println(this.host+":"+this.port);
                status = ConnectionStatus.CONNECTED;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        readHandshake();
        try {
            while (!exit) {
                Packet packet = (Packet) objectInputStream.readObject();
                System.out.println("PACKET RECEIVED!");
                switch (packet.getPacketType()) {
                    case MESSAGE: readMessage((MessagePacket) packet);
                    break;
                    case DISCONNECT:
                        acceptCloseConnection();
                        socket.shutdownOutput();
                        socket.shutdownInput();
                        socket.close();
                        status = ConnectionStatus.DISCONNECTED;
                        exit = true;
                        client.quit();
                        break;
                    case OK:
                        if (packet.getData().equals("DISCONNECT_OK") && status.equals(ConnectionStatus.AWAIT_DISCONNECT)){
                            socket.shutdownOutput();
                            socket.shutdownInput();
                            socket.close();
                            client.quit();
                            status = ConnectionStatus.DISCONNECTED;
                            exit = true;
                        }
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void sendPacket(Packet packet) throws IOException {
        System.out.println("WRITING PACKET:"+packet.toString());
        objectOutputStream.writeObject(packet);
    }

    public void acceptCloseConnection() throws IOException {
        SuccessPacket packet = new SuccessPacket(host, client.name, port, true, "DISCONNECT_OK");
        objectOutputStream.writeObject(packet);
        chatHandler.closeApp();
    }

    public void initCloseConnection() throws IOException {
        DisconnectPacket packet = new DisconnectPacket(new ClientData(client.name, host, port));
        status = ConnectionStatus.AWAIT_DISCONNECT;
        objectOutputStream.writeObject(packet);
    }

    public void forceCloseConnection() {
        exit = true;
        try {
            objectInputStream.close();
            objectOutputStream.close();
            socket.close();
            status = ConnectionStatus.DISCONNECTED;
        } catch (IOException ignored) {}
    }

    public String getName() {
        return name;
    }
}
