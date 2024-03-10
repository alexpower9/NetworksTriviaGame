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

            ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();

            while(true) //accept clients
            {
                Socket socket = server.accept();
                ClientHandler clientHandler = new ClientHandler(socket, udpSocket); //each clientHandler handles input/output for a client
                clients.add(clientHandler);
                new Thread(clientHandler).start();

                System.out.println("New Client connected"); //this is working, which is good
            }
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e);
        }
    }
}
