package Server;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final int ID;
    private ObjectOutputStream outputStream;
    private DataInputStream inputStream;
    private int correctAnswer = -1;
    private final ConcurrentLinkedQueue<Poll> pollQueue;
    private boolean isAnswerReceived = false;
    private boolean pollButtonPressed = true; // Track if the client has pressed the "Poll" button
    private int firstPollClientID = -1; // Track the client ID that pressed the "Poll" button first


    public ClientHandler(Socket clientSocket, int ID, ConcurrentLinkedQueue<Poll> pollQueue) {
        this.clientSocket = clientSocket;
        this.ID = ID;
        this.pollQueue = pollQueue;
    }

    @Override
    public void run() {
        try {
            initializeStreams();
            sendClientID();
            sendQuestionFile(1);
            handleClientResponses();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStreams();
        }
    }

    private void sendAcknowledgment(String acknowledgmentType) throws IOException {
        outputStream.writeObject(acknowledgmentType);
        outputStream.flush();
        System.out.println("Acknowledgment sent to client " + this.ID + ": " + acknowledgmentType);
    }

    private void initializeStreams() throws IOException {
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        inputStream = new DataInputStream(clientSocket.getInputStream());
    }

    private void sendClientID() throws IOException {
        outputStream.writeInt(this.ID);
        outputStream.flush();
    }

    void sendQuestionFile(int questionNumber) throws IOException {
        String filePath = "src/QuestionFiles/question" + questionNumber + ".txt";
        File file = new File(filePath);
        try (Scanner scanner = new Scanner(file)) {
            String msgType = "File";
            outputStream.writeObject(msgType);
            outputStream.writeInt(questionNumber);
            int counter = 0;
            while (counter < 5 && scanner.hasNextLine()) {
                String line = scanner.nextLine();
                outputStream.writeObject(line);
                counter++;
            }
            if (scanner.hasNextInt()) {
                correctAnswer = scanner.nextInt();
            }
            outputStream.flush();
        }
    }

    private void handleClientResponses() throws IOException {
        while (true) {
            // Read the "Poll" button press status from the client
            pollButtonPressed = inputStream.readBoolean();
    
            System.out.println("Client " + this.ID + " pressed Poll button: " + pollButtonPressed);
    
            // If the "Poll" button is pressed and the user is added to a queue
            if (pollButtonPressed) {
                pollQueue.add(new Poll(this.ID, 1)); // Add client to the poll queue
                // Check if the client is the first one in the queue
                if (!pollQueue.isEmpty() && pollQueue.peek().getID() == this.ID) {
                    sendAcknowledgment("ack"); // Send acknowledgment to the first client in the queue
                    System.out.println("Acknowledgment 'ack' sent to client " + this.ID);
                } else {
                    sendAcknowledgment("negative-ack"); // Send negative acknowledgment to other clients
                    System.out.println("Negative acknowledgment 'negative-ack' sent to client " + this.ID);
                }
            }
    
            // Only the first client in the queue should process the answer
            if (!pollQueue.isEmpty() && pollQueue.peek().getID() == this.ID) {
                // Read the answer from the client
                int currAnswer = inputStream.readInt();
    
                // Process the answer
                System.out.println("The answer given by client " + this.ID + ": " + currAnswer + ". The correct answer is: " + correctAnswer);
                int score = (currAnswer == correctAnswer) ? 10 : -10;
                outputStream.writeObject("Score");
                outputStream.writeInt(score);
                outputStream.flush();
                isAnswerReceived = true;
    
                // Remove the client from the queue after answering
                pollQueue.poll();
            }
    
            // Flush the output stream to ensure acknowledgment is sent immediately
            outputStream.flush();
        }
    }

    private void closeStreams() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAnswerReceived() {
        return isAnswerReceived;
    }

    public int getClientID(){
        return this.ID;
    }

    public void setPollButtonPressed(boolean pollButtonPressed) {
        this.pollButtonPressed = pollButtonPressed;
    }

}
