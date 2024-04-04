package Server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import SwingWindow.AppWindow;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JFrame;

public class Server
{   
    private static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
    public static boolean startGame = false;
    public static boolean duringRound = true;
    
    public static void printIP() throws IOException
    {
        Socket s = new Socket();
        s.connect(new InetSocketAddress("google.com", 80));
        String ip = s.getLocalAddress().getHostAddress();
        s.close();
        System.out.println("Current IP is " + ip);
    }

    public static void main(String[] args)
    {
        try
        {
            //print ip
            printIP();

            //so we have tcp connection and udp connection
            ServerSocket server = new ServerSocket(1234);
            DatagramSocket udpSocket = new DatagramSocket(1234);
            List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<>());

            UDPThread udpThread = new UDPThread(udpSocket);
            new Thread(udpThread).start(); //start the udp thread

            openGUI();

            while(true) //accept clients
            {
                Socket socket = server.accept();
                ClientHandler clientHandler = new ClientHandler(socket); //each clientHandler handles input/output for a client
                clients.add(clientHandler);
                new Thread(clientHandler).start();
                System.out.println("New Client connected"); //this is working, which is good

                //just testing to see if we can send a question to the client and have the client change states as we need it, which it does
                for (ClientHandler client : clients)
                {
                    client.sendMessage("STATE:AWAITING_GAME_START");
                    Thread.sleep(3000);
                    
                    // client.sendQuestion("src/QuestionFiles/question1_.txt");
                    
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e);
        }
        
    }

    public static void openGUI()
    {
        JFrame frame = new JFrame("Game Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);

        JButton button = new JButton("Start Game");
        button.addActionListener(e -> {
            startGame = true;
        });

        frame.getContentPane().add(button);

        frame.setVisible(true);
    }
}
