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

    //call this with the input from the java swing form
    public Client(Socket tcpSocket, DatagramSocket udpSocket, String serverIP)  
    {
        this.tcpSocket = tcpSocket;
        this.udpSocket = udpSocket;
        this.serverIP = serverIP;
        this.port = 1234;
    } 
    
    public void listenForQuestions() throws IOException
    {
        try
        {
            InputStream in = tcpSocket.getInputStream();

            while(true)
            {
                byte[] buffer = new byte[1024];
                in.read(buffer);
                String question = new String(buffer);
                System.out.println(question);
                break; //once we get a question we can break out of the loop
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
        }
        catch (Exception e)
        {
            System.out.println("Something went wrong: " + e);
        }
    }
}
