package Server;

import java.net.InetAddress;

public class Message 
{
    private String message;
    private InetAddress senderAddress;
    private int senderPort;

    public Message(String message, InetAddress senderAddress, int senderPort)
    {
        this.message = message;
        this.senderAddress = senderAddress;
        this.senderPort = senderPort;
    }

    public String getMessage()
    {
        return message;
    }
}
