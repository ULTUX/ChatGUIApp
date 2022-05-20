package server;

import exceptions.UnknownPacketException;
import packets.DisconnectPacket;
import packets.HandshakePacket;
import packets.Packet;
import utils.ClientData;
import utils.ConnectionStatus;
import utils.SocketResources;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;


public class ServerClientPairManager {
    final ClientData client2;
    final SocketResources client1Socket;
    final SocketResources client2Socket;
    final ConnectionStatus status;

    final Thread client1Thread;
    final Thread client2Thread;

    boolean exit = false;
    final Server server;

    public ServerClientPairManager(Server server, ClientData client1, ClientData client2) throws IOException, UnknownPacketException {
        this.client2 = client2;
        this.status = ConnectionStatus.NOT_GREET;
        this.server = server;

        var temp = new Socket(client1.getHostName(), client1.getPort());
        this.client1Socket = new SocketResources(temp, new ObjectOutputStream(temp.getOutputStream()), new ObjectInputStream(temp.getInputStream()));
        temp = new Socket(client2.getHostName(), client2.getPort());
        this.client2Socket = new SocketResources(temp, new ObjectOutputStream(temp.getOutputStream()), new ObjectInputStream(temp.getInputStream()));

        sendPacket(new HandshakePacket(InetAddress.getLocalHost().getHostAddress(), client1.getName(), client1.getPort()), client2Socket);
        sendPacket(new HandshakePacket(InetAddress.getLocalHost().getHostAddress(), client2.getName(), client2.getPort()), client1Socket);


        System.out.println("Paired with both clients!!");

        client1Thread = new Thread(handlePacket(this.client1Socket, client2Socket));
        client1Thread.start();
        client2Thread = new Thread(handlePacket(client2Socket, this.client1Socket));
        client2Thread.start();
        System.out.println("initialized forwarding threads");

    }

    public void sendPacket(Packet packet, SocketResources socket) throws IOException {
        System.out.println("WRITING PACKET:"+packet.toString());
        socket.out().writeObject(packet);
    }

    private Runnable handlePacket(SocketResources from, SocketResources to) {
        return () -> {
            try {
                ObjectInputStream input = from.in();
                ObjectOutputStream output = to.out();
                boolean interrupted = false;
                while (true) {
                    var data = input.readObject();
                    output.writeObject(data);
                    if (data instanceof DisconnectPacket) break;
                    if (Thread.interrupted()) {
                        interrupted = true;
                        break;
                    }
                }
                // If that was the first thread that exited from loop (to make it not run the same code twice)
                if (!interrupted) {
                    server.removePairManager(this);
                    if (client1Thread.getId() != Thread.currentThread().getId()) client1Thread.interrupt();
                    else client2Thread.interrupt();
                    //Thread is going to stop now.
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        };
    }

}
