package Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.Socket;

/*
 * We will use this class to handle sending and recieving messages from the client
 */
public class ClientHandler implements Runnable
{
    private Socket tcpSocket;
    private DatagramSocket udpSocket;

    public ClientHandler(Socket tcpSocket, DatagramSocket udpSocket)
    {
        this.tcpSocket = tcpSocket;
        this.udpSocket = udpSocket;
    }

    @Override
    public void run()
    {

    }

    //since we are sending questions as a text file to the client
    public void sendQuestion(String path)
    {
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

    public void handleUDP()
    {
        //handle the UDP connection here

        //my idea is to make it so that when we call this method, maybe we give it like 20 seconds
        //in a loop of calling this method. Then, we can see which ones didnt answer and put them at
        //the back of the queue maybe?
    }
}
