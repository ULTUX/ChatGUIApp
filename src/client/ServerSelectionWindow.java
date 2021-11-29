package client;

import utils.ClientData;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ServerSelectionWindow extends JFrame {
    private final Font FONT = new Font("Calibri", Font.PLAIN, 14);
    DefaultListModel<ClientData> clientDataList = new DefaultListModel<>();
    JList<ClientData> clientList = new JList<>(clientDataList);
    JScrollPane clientListScroll = new JScrollPane(clientList);
    public JButton connectButton = new JButton("Połącz");
    public JButton closeButton = new JButton("Anuluj");
    private JPanel panel = new JPanel();
    private Client clientInstance;
    public ServerSelectionWindow(String title, Client client) throws HeadlessException {
        super(title);
        this.clientInstance = client;
        setSize(500, 430);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        clientList.setSize(new Dimension(480, 330));
        clientListScroll.setPreferredSize(new Dimension(480, 330));
        closeButton.setPreferredSize(new Dimension(75, 30));
        connectButton.setPreferredSize(new Dimension(75, 30));
        closeButton.addActionListener(clientInstance);
        connectButton.addActionListener(clientInstance);

        panel.add(clientListScroll);
        panel.add(closeButton);
        panel.add(connectButton);
        setFont();
        setContentPane(panel);
        setVisible(true);

        client.getClientList().forEach(data -> {
            clientDataList.addElement(data);
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                setVisible(false);
                dispose();
                clientInstance.frame.setVisible(true);
            }
        });

    }

    private void setFont(){
        for (Component component : panel.getComponents()) {
            component.setFont(FONT);
        }
    }
}
