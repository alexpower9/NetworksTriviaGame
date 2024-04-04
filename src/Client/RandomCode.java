package Client;

import java.io.IOException;
import java.io.InputStream;

public class RandomCode
{
    public void listenForData() throws IOException
    {
        try
        {
            InputStream in = tcpSocket.getInputStream();

            while(true)
            {
                byte[] buffer = new byte[4096];
                int bytesRead = in.read(buffer);
                String message = new String(buffer, 0, bytesRead);
                System.out.println(message);

                if(bytesRead > 0)
                {
                    String dummyMessage = new String(buffer, 0, bytesRead);
                    System.out.println("Received message: " + dummyMessage); // Debugging: Print received message

                    if (message.startsWith("STATE:") && message.length() > 6)
                    {
                        String state = message.substring(6).trim(); //prefix all states with STATE: from server
                        System.out.println("State: " + state);
                        switch (state) {
                            case "AWAITING_GAME_START":
                                changeState(ClientState.AWAITING_GAME_START, "Waiting for game to start", null);
                                break;
                            case "POLL_WON":
                                changeState(ClientState.POLL_WON, "You won the poll!", null);
                                break;
                            case "POLL_LOST":
                                changeState(ClientState.POLL_LOST, "Unfortunately, you lost the poll. Please wait for the next round!", null);
                                break;
                            case "NO_POLL":
                                changeState(ClientState.NO_POLL, "Nobody wanted to answer. Weird! Wait for next round", null);
                                break;
                                // else if(message.startsWith("POLL_WON"))
                                // {
                                //     changeState(ClientState.POLL_WON, "You won the poll!", null);
                                // }
                                // else if(message.startsWith("POLL_LOST"))
                                // {
                                //     changeState(ClientState.POLL_LOST, "Unfortunately, you lost the poll. Please wait for the next round!", null);
                                // }
                                // else if(message.startsWith("NO_POLL"))
                                // {
                                //     changeState(ClientState.NO_POLL, message, null);
                                // }
                            }
                    }
                    else if (message.startsWith("QUESTION:"))
                    {
                        // System.out.println(message);
                        // String question = message.substring(9).trim();
                        // String[] questionFile;
                        String[] lines = message.split("\n");
                        String question = lines[1].trim(); // The question is on the second line
                        String[] questionFile = new String[4];
                        questionFile[0] = lines[2].trim(); // The first possible answer is on the third line
                        questionFile[1] = lines[3].trim(); // The second possible answer is on the fourth line
                        questionFile[2] = lines[4].trim(); // The third possible answer is on the fifth line
                        questionFile[3] = lines[5].trim(); // The fourth possible answer is on the sixth line
                        changeState(ClientState.QUESTION_RECIEVED, question, questionFile);
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error in changing state message: " + e);
        }
    }
}
