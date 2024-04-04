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

    public void setScore(int score)
    {
        this.score = score;
    }

    private void changeState(ClientState state, String message, String[] questionFile, String winnerOrLoser)
    {
        if (observer != null)
        {
            observer.onClientStateChanged(state, message, questionFile, winnerOrLoser);
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
            BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

            String message;
            while((message = in.readLine()) != null)
            {
                if (message.startsWith("STATE:") && message.length() > 6)
                {
                    String state = message.substring(6).trim(); //prefix all states with STATE: from server
                    switch (state) {
                        case "AWAITING_GAME_START":
                            changeState(ClientState.AWAITING_GAME_START, "Waiting for game to start", null,null);
                            break;
                        case "NO_POLL":
                            changeState(ClientState.NO_POLL, "Nobody wanted to answer. Weird! Wait for next round", null, null);
                            break;
                        case "NO_ANSWER":
                            changeState(ClientState.NO_ANSWER, "You didnt answer! You lose 20 points for that", null, null);
                            break;
                        case "ANSWER_CORRECT":
                            changeState(ClientState.ANSWER_CORRECT, "You answered correctly! You get 10 points", null, null);
                            break;
                        case "ANSWER_INCORRECT":
                            changeState(ClientState.ANSWER_INCORRECT, "You answered incorrectly! You lose 10 points", null, null);
                            break;
                        case "NEXT_QUESTION":
                            changeState(ClientState.NEXT_QUESTION, "Next question is coming up", null, null);
                            break;
                    }
                }
                else if (message.startsWith("QUESTION:"))
                {
                    // System.out.println(message);
                    // String question = message.substring(9).trim();
                    // String[] questionFile;
                    String[] lines = message.split("\\*");
                    // for (String line : lines)
                    // {
                    //     System.out.println(line);
                    // }

                    String question = lines[1].trim(); // The question is on the second line
                    String[] questionFile = new String[4];
                    questionFile[0] = lines[2].trim(); // The first possible answer is on the third line
                    questionFile[1] = lines[3].trim(); // The second possible answer is on the fourth line
                    questionFile[2] = lines[4].trim(); // The third possible answer is on the fifth line
                    questionFile[3] = lines[5].trim(); // The fourth possible answer is on the sixth line
                    
                    if(lines[lines.length - 1].equals("WINNER"))
                    {
                        changeState(ClientState.QUESTION_RECIEVED, question, questionFile, "WINNER");
                    }
                    else if(lines[lines.length - 1].equals("LOSER"))
                    {
                        changeState(ClientState.QUESTION_RECIEVED, question, questionFile, "LOSER");
                    }
                    else
                    {
                        changeState(ClientState.QUESTION_RECIEVED, question, questionFile, "NORMAL");
                    }
                }
                else if(message.startsWith("GET_SCORE"))
                {
                    try 
                    {
                        PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
                        out.println(String.valueOf(this.getScore()));
                    } catch (Exception e)
                    {
                        System.out.println("Error Sending Time Out: " + e);
                    }
                }
                else if(message.startsWith("POSITION:"))
                {
                    String position = message.substring("POSITION:".length()).trim();
                    changeState(ClientState.POSITION_RECIEVED, position, null, null);
                }
                else if(message.startsWith("KILL"))
                {
                    System.exit(0);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error in changing state message: " + e);
        }
    }

    public int getScore()
    {
        return this.score;
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
            PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
            out.println("TIMEOUT");
        } catch (Exception e)
        {
            System.out.println("Error Sending Time Out: " + e);
        }
    }

    public void sendAnswer(String answer)
    {
        try 
        {
            PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
            out.println(answer);
        } catch (Exception e)
        {
            System.out.println("Error Sending Time Out: " + e);
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
