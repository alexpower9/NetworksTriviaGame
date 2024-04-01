package Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server{

    private ConcurrentLinkedQueue<Poll> pollQueue;
    private ServerSocket serverSocket;
    private boolean gameOver;
    private int clientIDs;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public Server(int port){
        gameOver = false;
        pollQueue = new ConcurrentLinkedQueue<>();
        this.clientIDs = 0;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String ip = localHost.getHostAddress();
            System.out.println("Current IP is " + ip);
            serverSocket = new ServerSocket(port);
            System.out.println("Server is running");
        } 
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void startServer(){      

        executorService.submit(new UDPThread(12345,pollQueue));
        executorService.submit(new PollQueue(pollQueue));
        
        while (!gameOver){
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                clientSocket.setReuseAddress(true);
                System.out.println("A new client has connected: " + clientSocket);
                clientIDs++;
                System.out.println(clientIDs);
                executorService.submit(new ClientHandler(clientSocket, clientIDs, pollQueue));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }




    public static void main(String[] args){
        
        Server server = new Server(1234);
        server.startServer();


    }
}