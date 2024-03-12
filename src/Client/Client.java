package Client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;

import java.io.*;
import SwingWindow.AppWindow;

public class Client
{
    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private String serverIP; //better to have this so we can send udp packets later easily
    private int port;
    private ClientStateObserver observer;

    //call this with the input from the java swing form
    public Client(Socket tcpSocket, DatagramSocket udpSocket, String serverIP)  
    {
        this.tcpSocket = tcpSocket;
        this.udpSocket = udpSocket;
        this.serverIP = serverIP;
        this.port = 1234;
    } 

    public void setObserver(ClientStateObserver observer)
    {
        this.observer = observer;
    }

    private void changeState(ClientState state, String message)
    {
        if (observer != null)
        {
            observer.onClientStateChanged(state, message);
        }
    }
    
    /*
     * My idea for this method is to handle all the input from the server.
     * If its a state change from the server, then we can call the changeState method
     * If its a question, then we can use the info from it and update the window, and so on.
     * Not exactly sure how this is going to work, but worth a shot.
     */
    public void listenForData() throws IOException
    {
        try
        {
            InputStream in = tcpSocket.getInputStream();

            while(true)
            {
                byte[] buffer = new byte[1024];
                int bytesRead = in.read(buffer);
                String message = new String(buffer, 0, bytesRead);

                if (message.startsWith("STATE:"))
                {
                    String state = message.substring(6).trim(); //prefix all states with STATE: from server
                    System.out.println("State: " + state);
                    switch (state) {
                        case "AWAITING_GAME_START":
                            changeState(ClientState.AWAITING_GAME_START, "Waiting for game to start");
                            break;
                    }
                }
                else if (message.startsWith("QUESTION:"))
                {
                    String question = message.substring(9).trim();
                    changeState(ClientState.QUESTION_RECIEVED, question);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e);
        }
    }

    public void sendPull()
    {
        try
        {
            byte[] pull = "pull".getBytes();
            DatagramPacket pullPacket = new DatagramPacket(pull, pull.length, InetAddress.getByName(this.serverIP), 1234);
            udpSocket.send(pullPacket);
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e);
        }
    }

    public void sendAnswer(String answer)
    {
        try
        {
            byte[] answerBytes = answer.getBytes();
            DatagramPacket answerPacket = new DatagramPacket(answerBytes, answerBytes.length, InetAddress.getByName(this.serverIP), 1234);
            udpSocket.send(answerPacket);
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e);
        }
    }

    public String getServerIP()
    {
        return this.serverIP;
    }

    public static void main(String[] args)
    {
        try
        {
            //we can move at lot of this into a method later to make it easier
            AppWindow window = new AppWindow();
            String ip = window.getIpFutureTask().get();
            
            //now make new client
            Client client = new Client(new Socket(ip, 1234), new DatagramSocket(), ip);
            window.setClient(client);
            client.setObserver(window);
            new Thread(() -> {
                try {
                    client.listenForData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        catch (Exception e)
        {
            System.out.println("Something went wrong: " + e);
        }
    }
}
