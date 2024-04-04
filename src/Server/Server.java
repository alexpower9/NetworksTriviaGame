package Server;

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

import SwingWindow.AppWindow;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JFrame;

public class Server
{   
    public static volatile boolean startGame = false;
    private static volatile boolean hasAnswered = false;
    public static boolean duringRound = true;
    public static List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<>());
    private static String correctAnswer = "A"; //just test with this
    
    public static void printIP() throws IOException
    {
        Socket s = new Socket();
        s.connect(new InetSocketAddress("google.com", 80));
        String ip = s.getLocalAddress().getHostAddress();
        s.close();
        System.out.println("Current IP is " + ip);
    }

    public static void main(String[] args)
    {
        try
        {
            //print ip
            printIP();

            //so we have tcp connection and udp connection
            ServerSocket server = new ServerSocket(1234);
            DatagramSocket udpSocket = new DatagramSocket(1234);

            UDPThread udpThread = new UDPThread(udpSocket);
            new Thread(udpThread).start(); //start the udp thread

            openGUI();

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
                        Socket socket = server.accept();
                        ClientHandler clientHandler = new ClientHandler(socket);
                        clientHandlers.add(clientHandler);
                        new Thread(clientHandler).start();
                        System.out.println("New Client connected");
            
                        for (ClientHandler client : clientHandlers) {
                            client.sendMessage("STATE:AWAITING_GAME_START");
                            Thread.sleep(3000);
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // Thread for starting the game
            new Thread(() -> {
                while(true)
                {
                    if (startGame) {
                        

                        int clientSize = clientHandlers.size(); //this way, if someone joins mid round it wont mess it up
                        ExecutorService executor = Executors.newFixedThreadPool(clientSize); //create a thread pool for each client
                        for (ClientHandler client : clientHandlers) {
                            // Submit a task to the ExecutorService for each client
                            executor.submit(() -> {
                                // Send the question
                                try {
                                    client.sendQuestion("src/QuestionFiles/question1_.txt");
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            });
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
                            System.out.println("Nothing was in the queue.");
                            id = null;
                        }
                        else
                        {
                            System.out.println("Something was found");
                            id = msg.getMessage();
                        }

                        // for (ClientHandler client : clientHandlers) {
                        //     executor.submit(() -> {
                        //         try {
                        //             if (msg == null) {
                        //                 client.sendMessage("STATE:NO_POLL");
                        //                 System.out.println("Sent a no pull to client");
                        //             } else if (client.getId().equals(id)) {
                        //                 client.sendWinnerQuestion("src/QuestionFiles/question1_.txt");
                        //                 String response = client.readResponse();
                        //                 hasAnswered = true;
                        //                 if (response.equals(correctAnswer.toLowerCase())) {
                        //                     client.sendMessage("STATE:ANSWER_CORRECT");
                        //                 } else if (response.equals("No answer")){
                        //                     client.sendMessage("STATE:NO_ANSWER");
                        //                 } else {
                        //                     client.sendMessage("STATE:ANSWER_INCORRECT");
                        //                 }
                                       
                        //             } else {
                        //                 client.sendLoserQuestion("src/QuestionFiles/question1_.txt");
                        //                 client.sendMessage("STATE:NEXT_QUESTION");
                        //             }
                        //         } catch (IOException e) {
                        //             System.out.println("Error sending message: " + e);
                        //         }
                        //     });
                        // }

                        for(ClientHandler client : clientHandlers)
                        {
                            try {
                                if (msg == null) {
                                    client.sendMessage("STATE:NO_POLL");
                                    System.out.println("Sent a no pull to client");
                                } else if (client.getId().equals(id)) {
                                    client.sendWinnerQuestion("src/QuestionFiles/question1_.txt");
                                    System.out.println("Sent winner question");
                                    String response = client.readResponse();
                                    hasAnswered = true;
                                    if (response.equals(correctAnswer.toLowerCase())) {
                                        client.sendMessage("STATE:ANSWER_CORRECT");
                                    } else if (response.equals("No answer")){
                                        client.sendMessage("STATE:NO_ANSWER");
                                    } else {
                                        client.sendMessage("STATE:ANSWER_INCORRECT");
                                    }
                                    
                                } else {
                                    client.sendLoserQuestion("src/QuestionFiles/question1_.txt");
                                    client.sendMessage("STATE:NEXT_QUESTION");
                                }
                            } catch (IOException e) {
                                System.out.println("Error sending message: " + e);
                            }
                        }
                        
                        // Shutdown the ExecutorService and wait for all tasks to finish
                        executor.shutdown();
                        try {
                            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //wait 3 three seconds before starting the next round
                        try {
                            Thread.sleep(20000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        UDPThread.udpMessages.clear();
                        
                        //startGame = false;
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

        JButton button = new JButton("Start Game");
        button.addActionListener(e -> {
            startGame = true;
        });

        frame.getContentPane().add(button);

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
