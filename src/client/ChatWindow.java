package client;

import exceptions.ClientNotConnectedException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class ChatWindow extends JFrame implements ActionListener {
    private final Font FONT = new Font("Calibri", Font.PLAIN, 14);
    private PacketManager currentPacketManager;
    JTextArea hist = new JTextArea();
    JScrollPane histScroll = new JScrollPane(hist);
    JButton closeButton = new JButton("Zamknij połączenie");
    JPanel panel = new JPanel();
    JTextField toSend = new JTextField();
    Client parent;
    public ChatWindow(String title, Client client) throws HeadlessException {
        super(title);
        this.parent = client;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(300, 530);
        setAutoRequestFocus(true);
        toSend.setPreferredSize(new Dimension(280, 30));
        toSend.addActionListener(this);
        histScroll.setPreferredSize(new Dimension(280, 400));
        hist.setPreferredSize(new Dimension(280, 400));
        histScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        hist.setEditable(false);
        closeButton.addActionListener(this);
        panel.add(toSend);
        panel.add(hist);
        panel.add(closeButton);
        setFont();
        setContentPane(panel);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    currentPacketManager.initCloseConnection();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Nie można było zamknąć połączenia, wymuszanie zamknięcia.");
                    currentPacketManager.forceCloseConnection();
                }
                client.quit();
                setVisible(false);
                dispose();
            }
        });

    }

    public void setCurrentPacketManager(PacketManager currentPacketManager) {
        this.currentPacketManager = currentPacketManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(toSend)){
            try {
                currentPacketManager.sendMessage(toSend.getText());
                hist.append(parent.getName()+": "+toSend.getText()+"\n");
                toSend.setText("");
            } catch (ClientNotConnectedException | IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Nie można wysłać wiadomości.", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
        if (e.getSource().equals(closeButton)){
            try {
                currentPacketManager.initCloseConnection();
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Nie można było zamknąć połączenia, wymuszanie zamknięcia.");
                currentPacketManager.forceCloseConnection();
                closeApp();
            }
            closeApp();
        }
    }

    void closeApp() {
        setVisible(false);
        dispose();
    }

    private void setFont(){
        for (Component component : panel.getComponents()) {
            component.setFont(FONT);
        }
    }

    public void messageReceived(String data) {
        hist.append(currentPacketManager.getName()+": "+data+"\n");
    }
}
