package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UDPThread implements Runnable
{
    public static Queue<Message> udpMessages = new ConcurrentLinkedQueue<>();

    private DatagramSocket udpSocket;

    public UDPThread(DatagramSocket udpSocket)
    {
        this.udpSocket = udpSocket;
    }

    @Override
    public void run()
    {
        try
        {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true)
            {
                udpSocket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                Message msg = new Message(message, packet.getAddress(), packet.getPort());
                udpMessages.add(msg);
            }
        } catch (Exception e) {
            System.out.println("Error in UDPThread: " + e);
        }
    }
}
