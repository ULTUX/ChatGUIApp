package client;

import exceptions.ClientNotConnectedException;
import exceptions.UnknownPacketException;
import packets.*;
import utils.ClientData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Client implements Runnable, ActionListener{
    String name;
    JFrame frame = new JFrame("Oczekiwanie na połączenie...");
    JLabel infoLabel = new JLabel("Oczekiwanie na połączenie z innym klientem...");
    JButton connectButton = new JButton("Połącz");
    JPanel panel = new JPanel();
    private String serverAddress;
    private int PORT;
    private boolean exit = false;
    private ServerSocket serverSocket;
    private ArrayList<PacketManager> packetManagers = new ArrayList<>();
    private String hostName;
    private List<ClientData> clientList;
    private ServerSelectionWindow currentSelectionWindow;
    {
        hostName = InetAddress.getLocalHost().getHostAddress();
    }

    public Client() throws HeadlessException, UnknownPacketException, ClassNotFoundException, IOException {
        int port;
        try {
             port = Integer.parseInt(JOptionPane.showInputDialog("Proszę podać PORT klienta."));
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(frame, "Zły port, zamykanie zplilkacji...", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }
        PORT = port;
        name = JOptionPane.showInputDialog("Proszę podać nazwę klienta.");
        serverAddress = JOptionPane.showInputDialog("Proszę podać adres serwera.");
        if (serverAddress.equals("")) serverAddress = "localhost";
        frame.setSize(450, 80);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        connectButton.addActionListener(this);

        panel.add(infoLabel);
        panel.add(connectButton);
        frame.setContentPane(panel);
        frame.setVisible(true);
        new Thread(this).start();
        sendServerHandshake();
        getServerList();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                quit();
            }
        });
    }

    public void sendMessage(PacketManager manager, String message) throws IOException, ClientNotConnectedException {
        manager.sendMessage(message);
    }

    private void sendServerHandshake() throws UnknownPacketException {
        try {
            Socket socket = new Socket(serverAddress, 2122);
            if (socket.isConnected()) {
                System.out.println("CONNECTED");
                HandshakePacket packet = new HandshakePacket(InetAddress.getLocalHost().getHostAddress(), name, PORT);
                ObjectOutputStream oStream = new ObjectOutputStream(socket.getOutputStream());
                oStream.writeObject(packet);
                System.out.println("SENDING...");
                System.out.println(packet.toString());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getServerList() throws ClassNotFoundException {
        try {
            Socket socket = new Socket(serverAddress, 2122);
            if (socket.isConnected()) {
                System.out.println("CONNECTED");
                ObjectOutputStream oStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                GetClientsRequestPacket req = new GetClientsRequestPacket(hostName, name, PORT);
                oStream.writeObject(req);
                System.out.println("SENDING...");
                System.out.println(req.toString());
                Packet gotPacket = (Packet) inputStream.readObject();
                if (gotPacket.getPacketType().equals(PacketType.OK)){
                    GetClientsResponsePacket packet = (GetClientsResponsePacket) gotPacket;
                    this.clientList = packet.getReceivedClients();
                    System.out.println("Clients:"+clientList.toString());
                }

            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Najprawdopodobniej serwer już został zamknięty.","Błąd", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void connectToClient(ClientData data) throws IOException {
        int port = data.getPort();
        if (port != PORT){
            Socket socket = new Socket(serverAddress, 2122);
            if (socket.isConnected()){
                new ObjectOutputStream(socket.getOutputStream()).writeObject(new PairPacket(new ClientData(this.name, InetAddress.getLocalHost().getHostAddress(), PORT), data));
                System.out.println("CONNECTED TO SOCKET for pairing");
            }
        }
    }
    public static void main(String[] args) {
        try {
            new Client();
        } catch (UnknownPacketException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(connectButton)) {
            try {
                frame.setVisible(false);
                getServerList();
                if (currentSelectionWindow == null || !currentSelectionWindow.isDisplayable()) currentSelectionWindow = new ServerSelectionWindow("Wybór klienta", this);
                else currentSelectionWindow.setVisible(true);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Nie można było uzyskać listy klientów od serwera, może serwer jest wyłączony?");
            }
        }
        if (e.getSource().equals(currentSelectionWindow.closeButton)) {
            currentSelectionWindow.setVisible(false);
            currentSelectionWindow.dispose();
            frame.setVisible(true);
        }
        if (e.getSource().equals(currentSelectionWindow.connectButton)){
            currentSelectionWindow.setVisible(false);
            currentSelectionWindow.dispose();
            if (currentSelectionWindow.clientList.getSelectedIndex() == -1 || currentSelectionWindow.clientDataList.getElementAt(currentSelectionWindow.clientList.getSelectedIndex()) == null
            || currentSelectionWindow.clientDataList.getElementAt(currentSelectionWindow.clientList.getSelectedIndex()).equals(new ClientData(name, hostName, PORT))){
                JOptionPane.showMessageDialog(currentSelectionWindow, "Nie wybrano żadnego klienta do połączenia, lub wybrano tego klienta.", "Błąd", JOptionPane.ERROR_MESSAGE);
                currentSelectionWindow.setVisible(true);
            }
            else {
                ClientData selectedClient = currentSelectionWindow.clientDataList.getElementAt(currentSelectionWindow.clientList.getSelectedIndex());
                try {
                    connectToClient(selectedClient);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(currentSelectionWindow, "Wystąpił błąd podczas próby połaczenia z klientem");
                }
            }

        }
    }



    @Override
    public void run() {
        try {
            try {
                serverSocket = new ServerSocket(PORT);
            } catch (BindException e){
                JOptionPane.showMessageDialog(frame, "Adres już jest w użyciu!");
                quit();
                exit = true;
            }
            while (!exit){
                Socket socket = serverSocket.accept();
                if (socket != null){
                    System.out.println("SOMEONE CONNECTED TO SOCKET!");
                    packetManagers.add(new PacketManager(this, socket, new ChatWindow("Połączenie", this), null));
                    frame.setVisible(false);
                }
            }
        } catch (SocketException | UnknownPacketException ignored){}
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void quit() {
        sendGoodBye();
        exit = true;
        try {
            serverSocket.close();
        } catch (IOException ignored) {}
        frame.setVisible(false);
        frame.dispose();
    }

    public void closeAllSockets(){
        packetManagers.forEach(packetManager -> {
            try {
                packetManager.initCloseConnection();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Nie można było zamknąć połączenia, wymuszanie zamknięcia.");
                packetManager.forceCloseConnection();
            }
        });
    }

    private void sendGoodBye(){
        try {
            Socket socket = new Socket(serverAddress, 2122);
            if (socket.isConnected()) {
                DisconnectPacket packet = new DisconnectPacket(new ClientData(name, hostName, PORT));
                System.out.println("CONNECTED");
                ObjectOutputStream oStream = new ObjectOutputStream(socket.getOutputStream());
                oStream.writeObject(packet);
                System.out.println("SENDING...");
                System.out.println(packet.toString());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public String getHostName() {
        return hostName;
    }

    public int getPORT() {
        return PORT;
    }

    public List<ClientData> getClientList() {
        return clientList;
    }

    public String getName() {
        return name;
    }
}
