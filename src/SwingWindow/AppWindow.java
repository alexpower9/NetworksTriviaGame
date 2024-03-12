package SwingWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import Client.Client;
import Client.ClientState;
import Client.ClientStateObserver;

/*
 * Use this class to make a new window for the application. That way we can easily make a new window in
 * both our client and server classes. Not sure if this is the best way, but if we include methods to call
 * specifically for either class, it should be fine. Maybe we can just make a new JFrame class for the server though
 */

public class AppWindow extends JFrame implements ClientStateObserver
{
    private ArrayList<JLabel> labels = new ArrayList<JLabel>();
    private JTextField[] ipFields = new JTextField[4];
    private String stringIP = "";
    private FutureTask<String> ipFutureTask = new FutureTask<>(() -> stringIP); //block the thread until a valid IP is inputted
    private Client client;

    public AppWindow()
    {

        this.setSize(950, 540);
        this.setLocationRelativeTo(null); //opens in the middle of the screen
        this.setLayout(new BorderLayout());
        
        //add the opening label
        JLabel label = new JLabel("Welcome to the quiz! Please enter the server IP into the input field below.");
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setForeground(Color.BLACK);
        labels.add(label);
        JPanel labelPanel = new JPanel(new GridBagLayout());
        labelPanel.add(label);
        this.add(labelPanel, BorderLayout.NORTH);

        addIPFields();

        this.setVisible(true);
    }

    private void addIPFields() //this is a mess
    {   
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JPanel inputPanel = new JPanel(new FlowLayout());
        for(int i = 0; i < 4; i++)
        {
            ipFields[i] = new JTextField(3);
            inputPanel.add(ipFields[i]);

            if(i < 3) //dont add a . after the last column
            {
                JLabel dotLabel = new JLabel(".");
                inputPanel.add(dotLabel);
            }
        }
    
        JButton button = new JButton("Connect");
        inputPanel.add(button);
        panel.add(inputPanel, gbc);
        JPanel errorPanel = new JPanel(new GridBagLayout());
        JLabel errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);
        errorPanel.add(errorLabel);
        panel.add(errorPanel, gbc);
    
        button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                for(int i = 0; i < 4; i++)
                {
                    try
                    {
                        int num = Integer.parseInt(ipFields[i].getText());
                        if(num < 0 || num > 255)
                        {
                            throw new NumberFormatException();
                        }
                    } 
                    catch(NumberFormatException ex)
                    {
                        errorLabel.setText("Please enter a valid IP address. Each field should be a number between 0 and 255.");
                        panel.revalidate();
                        panel.repaint();
                        return;
                    }
                }
                AppWindow.this.stringIP = ipFields[0].getText() + "." + ipFields[1].getText() + "." + ipFields[2].getText() + "." + ipFields[3].getText();
                System.out.println("IP was successfully inputted");
                errorLabel.setText("");
                panel.revalidate();
                panel.repaint();
                ipFutureTask.run(); //should signal that a correct IP has been inputted
            }
        });
    
        this.add(panel, BorderLayout.CENTER);
    }

    //use this to create the client object
    public FutureTask<String> getIpFutureTask()
    {
        return ipFutureTask;
    }

    @Override
    public void onClientStateChanged(ClientState state, String message)
    {
        switch(state)
        {
            case AWAITING_GAME_START:
                waitingForGameStart();
                System.out.println("Awaiting game start");
                break;
            case AWAITING_QUESTION:
                System.out.println("Awaiting question");
                break;
            case QUESTION_RECIEVED:
                System.out.println("Question recieved");
                break;
        }
    
    }

    public void setClient(Client client)
    {
        this.client = client;
    }

    private void waitingForGameStart()
    {
        this.getContentPane().removeAll();
        this.revalidate();
        this.repaint();

        JLabel waitingLabel = new JLabel("Waiting for game to start");
        this.add(waitingLabel, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
}
