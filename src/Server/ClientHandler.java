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
            int currAnswer = inputStream.readInt();
            System.out.println("The answer given: " + currAnswer + ". The correct answer is: " + correctAnswer);
            int score = (currAnswer == correctAnswer) ? 10 : -10;
            outputStream.writeObject("Score");
            outputStream.writeInt(score);
            outputStream.flush();
            isAnswerReceived = true;
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
}
