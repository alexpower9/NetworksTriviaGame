package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/*
 * We will use this class to handle sending and recieving messages from the client
 */
public class ClientHandler implements Runnable
{
    private Socket tcpSocket;
    private String id;
    
    public ClientHandler(Socket tcpSocket) throws IOException
    {
        this.tcpSocket = tcpSocket;
        
        BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
        this.id = in.readLine();
    }

    @Override
    public void run()
    {
        
    }

    public String getId()
    {
        return id;
    }
    public void checkAnswer() throws IOException
    {
        String response = readResponse();
        Server.checkAnswer(this, response);
    }

    public String waitForTimeout() throws IOException
    {
        BufferedReader input = null;

        try {
            input = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

            String response;
            while((response = input.readLine()) != null) {
                if(response.equals("TIMEOUT")) {
                    System.out.println("Timeout was received: From ClientHandler Class");
                    return response;
                }
            }
        } catch (IOException e) {
            System.out.println("Connection was severed");
        } finally {
            
        }

        return null;
    }

    public void sendWinnerQuestion(String path) throws IOException
    {
        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));

        String finalString = "";
        String line;
        while ((line = br.readLine()) != null)
        {
            finalString += line + "*";
        }
        br.close();

        if (finalString.length() > 0) {
            finalString = finalString.substring(0, finalString.length() - 1);
        }

        String winnerString =  finalString + "*" + "WINNER";

        PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
        out.println(winnerString);
    }

    public void sendLoserQuestion(String path) throws IOException
    {
        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));

        String finalString = "";
        String line;
        while ((line = br.readLine()) != null)
        {
            finalString += line + "*";
        }
        br.close();

        if (finalString.length() > 0) {
            finalString = finalString.substring(0, finalString.length() - 1);
        }

        String winnerString =  finalString + "*" + "LOSER";

        PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
        out.println(winnerString);
    }

    //since we are sending questions as a text file to the client
    public void sendQuestion(String path) throws IOException
    {
        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));

        String finalString = "";
        String line;
        while ((line = br.readLine()) != null)
        {
            finalString += line + "*";
        }
        br.close();

        if (finalString.length() > 0) {
            finalString = finalString.substring(0, finalString.length() - 1);
        }

        PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
        System.out.println(finalString);
        out.println(finalString);
    }
    public void sendMessage(String message) throws IOException 
    {
        PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
        out.println(message);
    }

    public String readResponse() throws IOException 
    {
        BufferedReader input = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream())); 

        String response;
        while((response = input.readLine()) != null)
        {
            if(!response.isEmpty())
            {
                return response;
            }
        }

        return null;
    }

}
