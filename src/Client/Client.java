package Client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.io.*;
import SwingWindow.AppWindow;

public class Client
{
    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private String serverIP; //better to have this so we can send udp packets later easily
    private int port;
    private ClientStateObserver observer;
    private int score;
    private String id;

    //call this with the input from the java swing form
    public Client(Socket tcpSocket, DatagramSocket udpSocket, String serverIP) throws IOException  
    {
        this.tcpSocket = tcpSocket;
        this.udpSocket = udpSocket;
        this.serverIP = serverIP;
        this.port = 1234;
        this.score = 0;
        this.id = UUID.randomUUID().toString();

        PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true); //generate random id for client, send to server
        out.println(id);
        System.out.println(this.id);
    } 

    

    public void setObserver(ClientStateObserver observer)
    {
        this.observer = observer;
    }

    private void changeState(ClientState state, String message, String[] questionFile)
    {
        if (observer != null)
        {
            observer.onClientStateChanged(state, message, questionFile);
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
                System.out.println(message);

                if (message.startsWith("STATE:"))
                {
                    String state = message.substring(6).trim(); //prefix all states with STATE: from server
                    System.out.println("State: " + state);
                    switch (state) {
                        case "AWAITING_GAME_START":
                            changeState(ClientState.AWAITING_GAME_START, "Waiting for game to start", null);
                            break;
                    }
                }
                else if (message.startsWith("QUESTION:"))
                {
                    // System.out.println(message);
                    // String question = message.substring(9).trim();
                    // String[] questionFile;
                    String[] lines = message.split("\n");
                    String question = lines[1].trim(); // The question is on the second line
                    String[] questionFile = new String[4];
                    questionFile[0] = lines[2].trim(); // The first possible answer is on the third line
                    questionFile[1] = lines[3].trim(); // The second possible answer is on the fourth line
                    questionFile[2] = lines[4].trim(); // The third possible answer is on the fifth line
                    questionFile[3] = lines[5].trim(); // The fourth possible answer is on the sixth line
                    changeState(ClientState.QUESTION_RECIEVED, question, questionFile);
                }
                else if(message.startsWith("POLL_WON"))
                {
                    changeState(ClientState.POLL_WON, "You won the poll!", null);
                }
                else if(message.startsWith("POLL_LOST"))
                {
                    changeState(ClientState.POLL_LOST, "Unfortunately, you lost the poll. Please wait for the next round!", null);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e);
        }
    }

    public void sendPoll()
    {
        try
        {
            byte[] pull = (this.id + " buzz").getBytes();
            DatagramPacket pullPacket = new DatagramPacket(pull, pull.length, InetAddress.getByName(this.serverIP), 1234);
            udpSocket.send(pullPacket);
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e);
        }
    }

    public void sendTimeout()
    {
        try
        {
            OutputStream out = tcpSocket.getOutputStream();
            out.write("T".getBytes());
            out.flush();
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
            OutputStream out = tcpSocket.getOutputStream();
            out.write(answer.getBytes());
            out.flush();
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
