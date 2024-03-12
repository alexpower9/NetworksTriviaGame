package Server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import SwingWindow.AppWindow;

public class Server
{   
    private static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
    
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

            

            while(true) //accept clients
            {
                Socket socket = server.accept();
                ClientHandler clientHandler = new ClientHandler(socket, udpSocket); //each clientHandler handles input/output for a client
                clients.add(clientHandler);
                new Thread(clientHandler).start();
                System.out.println("New Client connected"); //this is working, which is good

                //just testing to see if we can send a question to the client and have the client change states as we need it, which it does
                for (ClientHandler client : clients)
                {
                    client.sendMessage("STATE:AWAITING_GAME_START");
                    Thread.sleep(1000);
                    client.sendQuestion("src/QuestionFiles/question1.txt");
                }
                
            }
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e);
        }
        
    }
}
