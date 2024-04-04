package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
        System.out.println("Client ID: " + id);
    }

    @Override
    public void run()
    {

    }

    //since we are sending questions as a text file to the client
    public void sendQuestion(String path)
    {
        System.out.println("Sent question");
        File file = new File(path);
        //easiest way is to use a byte array to send the file over and have the client deal with it on their end
        byte[] fileBytes = new byte[(int) file.length()];

        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream(file);
            fis.read(fileBytes);

            OutputStream os = tcpSocket.getOutputStream();
            os.write(fileBytes);
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e);
        }
        finally
        {
            try
            {
                if (fis != null)
                {
                    fis.close();
                }
            }
            catch (Exception e)
            {
                System.out.println("Error: " + e);
            }
        }
    }
    public void sendMessage(String message) throws IOException 
    {
        OutputStream out = tcpSocket.getOutputStream();
        out.write(message.getBytes());
        out.flush();
    }

    public String readResponse() throws IOException
    {
        String response = "empty";
        BufferedReader input = new BufferedReader(new java.io.InputStreamReader(tcpSocket.getInputStream())); 
        //listens for a response for client

        //we know a response will be coming, so this works here
        while((response = input.readLine()) != null)
        {
            if(!response.equals("empty"))
            {
                return response;
                //when we notice a response, we return it
            }
        }

        return response;
    }

}
