package Server;

import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import SwingWindow.AppWindow;
import java.net.*;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Server
{   
    public static volatile boolean startGame = false;
    public static boolean duringRound = false;
    public static List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<>());
    private static String correctAnswer = "A"; //just test with this
    
    public static String getIP() throws IOException
    {
        Socket s = new Socket();
        s.connect(new InetSocketAddress("google.com", 80));
        String ip = s.getLocalAddress().getHostAddress();
        s.close();
        //System.out.println("Current IP is " + ip);
        return ip;
    }

    private static String getCurrentAnswer(String filePath) throws FileNotFoundException, IOException
    {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        String lastLine = null;
        String line;
        while ((line = br.readLine()) != null) {
            lastLine = line;
        }
        return lastLine;
    }
    }

    public static void main(String[] args)
    {
        try
        {
            //print ip
            //printIP();

            //so we have tcp connection and udp connection
            ServerSocket server = new ServerSocket(1234);
            DatagramSocket udpSocket = new DatagramSocket(1234);

            UDPThread udpThread = new UDPThread(udpSocket);
            new Thread(udpThread).start(); //start the udp thread

            openGUI();

            List<ClientHandler> newClients = new ArrayList<>();

            // while(true) //accept clients
            // {
            //     Socket socket = server.accept();
            //     ClientHandler clientHandler = new ClientHandler(socket); //each clientHandler handles input/output for a client
            //     clientHandlers.add(clientHandler);
            //     new Thread(clientHandler).start();
            //     System.out.println("New Client connected"); //this is working, which is good

            //     //just testing to see if we can send a question to the client and have the client change states as we need it, which it does
            //     for (ClientHandler client : clientHandlers)
            //     {
            //         client.sendMessage("STATE:AWAITING_GAME_START");
            //         Thread.sleep(3000);
            //         //client.sendQuestion("src/QuestionFiles/question1_.txt");
            //     }

            //     if(startGame)

            //     {
            //         for (ClientHandler client : clientHandlers)
            //         {
            //             client.sendQuestion("src/QuestionFiles/question1_.txt");
            //         }
            //     }
            // }
            new Thread(() -> {
                while (true) {
                    try {

                        //if the client joins mid round, accept them, add to a list.
                        //then, in our game loop, we check at the start if this has clients, and add them
                        //if it does.

                        //from testing, it seems to handle clients leaving no problem!
                        if(duringRound)
                        {
                            Socket socket = server.accept();
                            ClientHandler clientHandler = new ClientHandler(socket);
                            clientHandler.sendMessage("STATE:AWAITING_GAME_START");
                            newClients.add(clientHandler);
                        }
                        else
                        {
                            Socket socket = server.accept();
                            ClientHandler clientHandler = new ClientHandler(socket);
                            clientHandler.sendMessage("STATE:AWAITING_GAME_START");
                            clientHandlers.add(clientHandler);
                            new Thread(clientHandler).start();
                            System.out.println("New Client connected");

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            new Thread(() -> {
                while(true)
                {
                    if (startGame)
                    {
                        for(int q = 1; q <= 20; q++)
                        {
                            if (!newClients.isEmpty())
                            {
                                clientHandlers.addAll(newClients);
                                newClients.clear();
                            }
                            duringRound = true;
                            String questionString = "src/QuestionFiles/question" + String.valueOf(q) + "_.txt";
                            int clientSize = clientHandlers.size(); //this way, if someone joins mid round it wont mess it up
                            ExecutorService executor = Executors.newFixedThreadPool(clientSize); //create a thread pool for each client
                            for (ClientHandler client : clientHandlers) {
                                // Submit a task to the ExecutorService for each client
                                executor.submit(() -> {
                                    // Send the question
                                    try {
                                        client.sendQuestion(questionString);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }

                            try {
                                correctAnswer = getCurrentAnswer(questionString).toLowerCase();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            
                            executor.shutdown();
                            try {
                                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //wait for all timers to finish
                            
                            //if someone could find a way so we dont need to keep repeating this code, that would be great.
                            executor = Executors.newFixedThreadPool(clientSize);

                            for (ClientHandler client : clientHandlers) {
                                // Submit a task to the ExecutorService for each client
                                executor.submit(() -> {
                                    try {
                                        // Wait for the client to timeout
                                        client.waitForTimeout();

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }

                            // Shutdown the ExecutorService and wait for all tasks to finish
                            executor.shutdown();
                            try {
                                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }                        
                        
                            Message msg = UDPThread.udpMessages.poll();
                            String id;
                            if(msg == null)
                            {
                                id = null;
                            }
                            else
                            {
                                id = msg.getMessage();
                            }

                            executor = Executors.newFixedThreadPool(clientSize);

                            for(ClientHandler client : clientHandlers)
                            {
                                executor.submit(() -> {
                                    try {
                                        if (msg == null) {
                                            client.sendMessage("STATE:NO_POLL");
                                        } else if (client.getId().equals(id)) {
                                            client.sendWinnerQuestion(questionString);
                                            String response = client.readResponse();
                                            
                                            if (response.toLowerCase().equals(correctAnswer.toLowerCase())) {
                                                client.sendMessage("STATE:ANSWER_CORRECT");
    
                                            } else if (response.equals("No answer")){
                                                client.sendMessage("STATE:NO_ANSWER");
                                            } else {
                                                client.sendMessage("STATE:ANSWER_INCORRECT");
                                            }
                                            
                                        } else {
                                            //client.sendLoserQuestion(questionString);
                                            client.sendMessage("STATE:NEXT_QUESTION");
                                        }
                                    } catch (IOException e) {
                                        System.out.println("Error sending message: " + e);
                                    }
                                });
                            }

                            executor.shutdown();
                            try {
                                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }   

                            duringRound = false;

                            UDPThread.udpMessages.clear();
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if(q == 20)
                            {
                                break;
                            }
                        }

                        TreeMap<Integer, List<ClientHandler>> scoreMap = new TreeMap<>(Collections.reverseOrder());

                        for(ClientHandler client : clientHandlers)
                        {
                            try 
                            {
                                client.sendMessage("GET_SCORE");
                                String response = client.readResponse();
                                int score = Integer.parseInt(response);
                                scoreMap.computeIfAbsent(score, k -> new ArrayList<>()).add(client);
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }

                        int position = 1;
                        for(Map.Entry<Integer, List<ClientHandler>> entry : scoreMap.entrySet())
                        {
                            List<ClientHandler> clients = entry.getValue();
                            for(ClientHandler client : clients)
                            {
                                try {
                                    client.sendMessage("POSITION:" + position);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            position += clients.size();
                        }
                        break;
                    }
                }
            }).start();
        
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e);
        }
        
    }

    public static void openGUI()
    {
        JFrame frame = new JFrame("Game Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new FlowLayout()); // Set layout manager to FlowLayout

        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(e -> {
            startGame = true;
            startButton.setEnabled(false);
        });

        JButton killButton = new JButton("Kill Game");
        killButton.addActionListener(e -> {
            for(ClientHandler client : clientHandlers) {
                try {
                    client.sendMessage("KILL");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        frame.getContentPane().add(startButton);
        frame.getContentPane().add(killButton);

        try {
            String ip = getIP();
            JLabel ipLabel = new JLabel("Current IP is " + ip);
            frame.getContentPane().add(ipLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        frame.setVisible(true);
    }

    public static void checkAnswer(ClientHandler client, String answer) throws IOException
    {
        if (answer.equals("A"))
        {
            client.sendMessage("STATE:POLL_WON");
        }
        else
        {
            client.sendMessage("STATE:POLL_LOST");
        }
    }
}
