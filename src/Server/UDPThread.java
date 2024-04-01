package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UDPThread implements Runnable {
    private int portNumber;
    private DatagramSocket datagramSocket;
    private ConcurrentLinkedQueue<Poll> pollQueue;

    public UDPThread(int portNumber, ConcurrentLinkedQueue<Poll> pollQueue){

        this.pollQueue = pollQueue;
        this.portNumber = portNumber;
        
        try {
            datagramSocket = new DatagramSocket(portNumber);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        byte[] data = new byte[5];
        while (true) {
            DatagramPacket packet = new DatagramPacket(data, data.length);
                try {
                    datagramSocket.receive(packet);
                    Integer clientID, questionNumber;
                    if (data[1] == 44) {
                        clientID = (int) data[0] - 48;
                        questionNumber = ((int) data[2] - 48) * 10 + (int) data[3] - 48;
                    } else {
                        clientID = ((int) data[0] - 48) * 10 + (int) data[1] - 48;
                        questionNumber = ((int) data[3] - 48) * 10 + (int) data[4] - 48;
                    }
                    pollQueue.add(new Poll(clientID, questionNumber));
                } catch (IOException e) {
                    e.printStackTrace();
            }
        }
    }
}