package server;

import exceptions.UnknownClientException;
import exceptions.UnknownPacketException;
import packets.*;
import utils.ClientData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

public class Server extends JFrame implements ActionListener, Runnable {
    private final Font FONT = new Font("Calibri", Font.PLAIN, 14);
    private final int PORT = 2122;
    DefaultListModel<ClientData> list = new DefaultListModel<>();
    private final JList<ClientData> clientList = new JList<>(list);
    private final JScrollPane clientScroll = new JScrollPane(clientList);
    private final JLabel clientLabel = new JLabel("połączeni klienci", SwingConstants.LEFT);
    JButton stopButton = new JButton("Stop");
    JLabel statusLabel = new JLabel("Status serwera", SwingConstants.CENTER);
    private final JPanel panel = new JPanel();
    private boolean exit = false;
    private ServerSocket serverSocket;
    private String hostName;

    private ArrayList<ServerClientPairManager> pairManagers = new ArrayList<>();

    {
        try {
            hostName = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            hostName = null;
        }
    }

    public Server(String title) throws HeadlessException {
        super(title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(360, 215);
        setResizable(false);
        clientScroll.setPreferredSize(new Dimension(250, 100));
        clientScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        stopButton.setPreferredSize(new Dimension(60, 40));
        clientLabel.setPreferredSize(new Dimension(320, 30));
        clientLabel.setFont(FONT);
        stopButton.addActionListener(this);
        statusLabel.setPreferredSize(new Dimension(310, 30));


        panel.add(clientLabel);
        panel.add(clientScroll);
        panel.add(stopButton);
        panel.add(statusLabel);

        add(panel);
        setFont();
        setVisible(true);
        new Thread(this).start();
        pairManagers = new ArrayList<>();
    }

    private void setFont() {
        for (Component comp : panel.getComponents()) {
            comp.setFont(FONT);
        }
    }

    public static void main(String[] args) {
        new Server("Server");
    }

    private void quit() throws IOException {
        exit = true;
        serverSocket.close();
        pairManagers.forEach(ServerClientPairManager::dispose);
        setVisible(false);
        dispose();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(stopButton)) {
            try {
                quit();
            } catch (IOException ioException) {
                JOptionPane.showMessageDialog(this, "Nie można było zamknąć gniazdka serwera.", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            statusLabel.setText("Serwer działa na porcie: " + PORT);
            while (!exit) {
                Socket socket = serverSocket.accept();
                manageClientConnection(socket);

            }
        } catch (IOException ignored) {
        }
    }

    public void removeClient(ClientData client) {
        if (list.contains(client)) list.removeElement(client);
    }

    public void addClient(ClientData client) {
        if (!list.contains(client)) list.addElement(client);
        System.out.println(client.toString());
    }

    public void removePairManager(ServerClientPairManager manager) {
        pairManagers.remove(manager);
    }

    public void manageClientConnection(Socket socket) {
        try (
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())
        ) {
            Packet gotPacket = (Packet) input.readObject();

            switch (gotPacket.getPacketType()) {
                case HANDSHAKE:
                    addClient(new ClientData(gotPacket.getName(), gotPacket.getHostName(), gotPacket.getPort()));
                    break;
                case GET_DATA:
                    if (isClientConnected(gotPacket.getName(), gotPacket.getHostName(), gotPacket.getPort())) {
                        respondToGetPacket(output);
                    } else throw new UnknownClientException("That client is not connected with the server.");
                    break;
                case DISCONNECT:
                    if (isClientConnected(gotPacket.getName(), gotPacket.getHostName(), gotPacket.getPort())) {
                        DisconnectPacket dPacket = (DisconnectPacket) gotPacket;
                        removeClient(dPacket.getToDisconnect());

                    } else throw new UnknownClientException("That client is not connected with the server.");
                    break;
                case PAIR_CLIENT:
                    pairManagers.add(new ServerClientPairManager(this, new ClientData(gotPacket.getName(), gotPacket.getHostName(), gotPacket.getPort()), ((PairPacket) gotPacket).getToConnect()));

            }

        } catch (IOException | ClassNotFoundException | UnknownPacketException e) {
            JOptionPane.showMessageDialog(this, "Wystąpił błąd.", "Błąd", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                JOptionPane.showMessageDialog(this, "Wystąpił błąd.", "Błąd", JOptionPane.ERROR_MESSAGE);

            }
        } catch (UnknownClientException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Wystąpił błąd.", "Błąd", JOptionPane.ERROR_MESSAGE);

        }

    }

    private void respondToGetPacket(ObjectOutputStream stream) throws IOException, UnknownPacketException {
        Enumeration<ClientData> enumeration = list.elements();
        ArrayList<ClientData> response = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            ClientData data = enumeration.nextElement();
            response.add(data);
        }
        GetClientsResponsePacket packet = new GetClientsResponsePacket(response);
        packet.setPacketType(PacketType.OK);
        stream.writeObject(packet);
    }


    private void removeClient(String name, String hostname, int port) {
        Enumeration<ClientData> clientDataEnumeration = list.elements();
        ClientData toCompare = new ClientData(name, hostname, port);
        while (clientDataEnumeration.hasMoreElements()) {
            ClientData data = clientDataEnumeration.nextElement();
            if (data.equals(toCompare)) list.removeElement(data);
        }
    }


    private boolean isClientConnected(String name, String hostname, int port) {
        Enumeration<ClientData> elements = list.elements();
        ClientData clientData = new ClientData(name, hostname, port);
        while (elements.hasMoreElements()) {
            if (elements.nextElement().equals(clientData)) return true;
        }
        return false;
    }
}
