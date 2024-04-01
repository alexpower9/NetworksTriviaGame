package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

public class ClientMain extends JFrame implements ActionListener {
    private JTextField ipAddressField;
    private JButton connectButton;

    public ClientMain() {
        setTitle("Enter IP Address");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ipAddressField = new JTextField(20);
        connectButton = new JButton("Connect");
        connectButton.addActionListener(this);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));
        panel.add(new JLabel("Enter IP Address:"));
        panel.add(ipAddressField);
        panel.add(connectButton);

        add(panel);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connectButton) {
            String ipAddress = ipAddressField.getText();
            dispose(); 
            try {
                Client client = new Client(ipAddress, 1234);
                client.showWindow(); 
            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ClientMain();
    }
}
