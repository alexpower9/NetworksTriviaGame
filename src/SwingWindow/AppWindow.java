package SwingWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

import Client.Client;
import Client.ClientState;
import Client.ClientStateObserver;

/*
 * Use this class to make a new window for the application. That way we can easily make a new window in
 * both our client and server classes. Not sure if this is the best way, but if we include methods to call
 * specifically for either class, it should be fine. Maybe we can just make a new JFrame class for the server though
 */

public class AppWindow extends JFrame implements ClientStateObserver
{
    private ArrayList<JLabel> labels = new ArrayList<JLabel>();
    private JTextField[] ipFields = new JTextField[4];
    private String stringIP = "";
    private FutureTask<String> ipFutureTask = new FutureTask<>(() -> stringIP); //block the thread until a valid IP is inputted
    private Client client;

    private static JButton pollButton;
	private static JButton submitButton;
	private static JRadioButton optionButton[];
	private String optionButtonText[]; //To store text for each option
	private ButtonGroup optionButtonGroup;
	private int currentSelection;
	private JLabel question;
    String[] questionFile;
	private static JLabel timerLabel;
	private JLabel countdownLabel;
	private JLabel scoreLabel;
    private int scoreCount;
	private TimerTask clock;
    private static boolean isAnswering = false;

    public AppWindow()
    {
        this.setSize(1200, 800); 
        this.setLocationRelativeTo(null); 
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        //add the opening label
        JLabel label = new JLabel("Welcome to the quiz! Please enter the server IP into the input field below.");
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setForeground(Color.BLACK);
        labels.add(label);
        JPanel labelPanel = new JPanel(new GridBagLayout());
        labelPanel.add(label);
        this.add(labelPanel, BorderLayout.NORTH);

        addIPFields();

        this.setVisible(true);
    }

    private void addIPFields() //this is a mess
    {   
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JPanel inputPanel = new JPanel(new FlowLayout());
        for(int i = 0; i < 4; i++)
        {
            ipFields[i] = new JTextField(3);
            inputPanel.add(ipFields[i]);

            if(i < 3) //dont add a . after the last column
            {
                JLabel dotLabel = new JLabel(".");
                inputPanel.add(dotLabel);
            }
        }
    
        JButton button = new JButton("Connect");
        inputPanel.add(button);
        panel.add(inputPanel, gbc);
        JPanel errorPanel = new JPanel(new GridBagLayout());
        JLabel errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);
        errorPanel.add(errorLabel);
        panel.add(errorPanel, gbc);
    
        button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                for(int i = 0; i < 4; i++)
                {
                    try
                    {
                        int num = Integer.parseInt(ipFields[i].getText());
                        if(num < 0 || num > 255)
                        {
                            throw new NumberFormatException();
                        }
                    } 
                    catch(NumberFormatException ex)
                    {
                        errorLabel.setText("Please enter a valid IP address. Each field should be a number between 0 and 255.");
                        panel.revalidate();
                        panel.repaint();
                        return;
                    }
                }
                AppWindow.this.stringIP = ipFields[0].getText() + "." + ipFields[1].getText() + "." + ipFields[2].getText() + "." + ipFields[3].getText();
                System.out.println("IP was successfully inputted");
                errorLabel.setText("");
                panel.revalidate();
                panel.repaint();
                ipFutureTask.run(); //should signal that a correct IP has been inputted
            }
        });
    
        this.add(panel, BorderLayout.CENTER);
    }

    //use this to create the client object
    public FutureTask<String> getIpFutureTask()
    {
        return ipFutureTask;
    }

    @Override
    public void onClientStateChanged(ClientState state, String message, String[] questionFile, String winnerOrLoser)
    {
        switch(state)
        {
            case AWAITING_GAME_START:
                waitingForGameStart();
                System.out.println("Awaiting game start");
                break;
            case AWAITING_QUESTION:
                System.out.println("Awaiting question");
                break;
            case QUESTION_RECIEVED:
                questionRecieved(message, questionFile, winnerOrLoser);
                System.out.println("Question recieved");
                break;
            case NO_POLL:
                noPoll(message);
                break;
            case NO_ANSWER:
                noAnswer();
                break;
            case ANSWER_CORRECT:
                answerCorrect(message);
                break;
            case ANSWER_INCORRECT:
                answerIncorrect(message);
                break;
            case NEXT_QUESTION:
                nextQuestion(message);
                break;
        }
    
    }

    public void setClient(Client client)
    {
        this.client = client;
    }

    public Client getClient()
    {
        return client;
    }

    private void waitingForGameStart()
    {
        this.getContentPane().removeAll();
        this.revalidate();
        this.repaint();

        JLabel waitingLabel = new JLabel("Waiting for game to start");
        waitingLabel.setFont(new Font("Times New Roman", Font.BOLD, 32));
        waitingLabel.setBounds(500, 50, 1000, 100);
        this.add(waitingLabel);
    }

    //just a simple example to see
    //but we are going to have the parse the file and display the question/answers in a format that
    //is easy to read and interact with
    private void questionRecieved(String question, String[] questionFile, String winnerOrLoser)
    {
        if(winnerOrLoser.equals("WINNER"))
        {
            timerLabel = null;
            clock = null;
            isAnswering = true;
                //this.question = question;
            this.getContentPane().removeAll();
            this.revalidate();
            this.repaint();

            this.setSize(1200, 800); // Increased window size
            this.setLocationRelativeTo(null); // Center the window
            this.setLayout(null);
            this.setVisible(true);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setResizable(false);

            // JLabel questionLabel = new JLabel(question);
            // this.add(questionLabel, BorderLayout.CENTER);
            // this.revalidate();
            // this.repaint();
            JLabel winnerLabel = new JLabel("Take your pick!");
            winnerLabel.setFont(new Font("Times New Roman", Font.BOLD, 32));
            winnerLabel.setBounds(800, 200, 1000, 100);
            this.add(winnerLabel);

            this.questionFile = questionFile;
            timerLabel = new JLabel("Timer");
            timerLabel.setBounds(400, 20, 200, 50); // Adjusted size and position
            timerLabel.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
            timerLabel.setForeground(Color.BLUE); // Changed text color

            clock = new CustomTimerTask(10, this);
            Timer timer = new Timer();
            timer.schedule(clock, 0, 1000);

            scoreLabel = new JLabel("Score: " + scoreCount);
            scoreLabel.setBounds(50, 20, 200, 50);
            scoreLabel.setFont(new Font("Arial", Font.PLAIN, 24));

            countdownLabel = new JLabel("Time: ");
            countdownLabel.setBounds(300, 20, 200, 50); // Adjusted size and position
            countdownLabel.setFont(new Font("Arial", Font.PLAIN, 24));

            this.question = new JLabel(question);
            this.question.setFont(new Font("Times New Roman", Font.BOLD, 32));
            this.question.setBounds(50, 50, 1000, 100);
            this.add(this.question);

            pollButton = new JButton("Poll");
            pollButton.setBounds(50, 600, 200, 50); // Increased size
            pollButton.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
            pollButton.setEnabled(false);

            pollButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    client.sendPoll();
                }
            });
            
            // Initialize Submit Button
            submitButton = new JButton("Submit");
            submitButton.setBounds(300, 600, 200, 50);
            submitButton.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
            submitButton.setEnabled(true);
            //submitButton.addActionListener(this);

            optionButton = new JRadioButton[4];
            optionButtonText = new String[4];
            optionButtonGroup = new ButtonGroup();
            for(int index=0; index<optionButton.length; index++)
            {
                //String possibleAnswer = String.valueOf(questionFile[index].charAt(0));// Gets text from file for question
                optionButtonText[index] = questionFile[index]; // Gets text from file for options
                optionButton[index] = new JRadioButton(optionButtonText[index]);
                optionButton[index].setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
                optionButton[index].setBounds(50, 200+(index*80), 900, 60); // Adjusted size and position
                this.add(optionButton[index]);
                optionButtonGroup.add(optionButton[index]);
                optionButton[index].setEnabled(true); //change this when actually using it
            }

            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (JRadioButton button : optionButton) {
                        if (button.isSelected()) {
                            String selectedOption = button.getText();
                            ((CustomTimerTask) clock).onAnswerSubmitted();
                            String firstChar = String.valueOf(selectedOption.charAt(0));
                            System.out.println("We sent the answer: " + firstChar);
                            client.sendAnswer(firstChar);
                            ((CustomTimerTask) clock).resetAnswerSubmitted();
                            clock.cancel();
                            isAnswering = false;
                            break;
                        }
                    }
                }
            });

                this.add(timerLabel);
                this.add(scoreLabel);
                this.add(submitButton);
                this.add(countdownLabel);
                this.add(pollButton);
            }
        else if(winnerOrLoser.equals("LOSER"))
        {
            timerLabel = null;
            clock = null;
            isAnswering = false;
            //this.question = question;
            this.getContentPane().removeAll();
            this.revalidate();
            this.repaint();

            this.setSize(1200, 800); // Increased window size
            this.setLocationRelativeTo(null); // Center the window
            this.setLayout(null);
            this.setVisible(true);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setResizable(false);

            this.questionFile = questionFile;
            timerLabel = new JLabel("Timer");
            timerLabel.setBounds(400, 20, 200, 50); // Adjusted size and position
            timerLabel.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
            timerLabel.setForeground(Color.BLUE); // Changed text color

            clock = new CustomTimerTask(10, this);
            Timer timer = new Timer();
            timer.schedule(clock, 0, 1000);

            scoreLabel = new JLabel("Score: " + scoreCount);
            scoreLabel.setBounds(50, 20, 200, 50);
            scoreLabel.setFont(new Font("Arial", Font.PLAIN, 24));

            countdownLabel = new JLabel("Time: ");
            countdownLabel.setBounds(300, 20, 200, 50); // Adjusted size and position
            countdownLabel.setFont(new Font("Arial", Font.PLAIN, 24));

            this.question = new JLabel(question);
            this.question.setFont(new Font("Times New Roman", Font.BOLD, 32));
            this.question.setBounds(50, 50, 1000, 100);
            this.add(this.question);

            pollButton = new JButton("Poll");
            pollButton.setBounds(50, 600, 200, 50); // Increased size
            pollButton.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
            pollButton.setEnabled(false);

            pollButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    client.sendPoll();
                }
            });
            pollButton.setEnabled(false);
            
            // Initialize Submit Button
            submitButton = new JButton("Submit");
            submitButton.setBounds(300, 600, 200, 50);
            submitButton.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
            submitButton.setEnabled(false);
            //submitButton.addActionListener(this);

            optionButton = new JRadioButton[4];
            optionButtonText = new String[4];
            optionButtonGroup = new ButtonGroup();
            for(int index=0; index<optionButton.length; index++)
            {
                String possibleAnswer = String.valueOf(questionFile[index].charAt(0));// Gets text from file for question
                optionButtonText[index] = questionFile[index]; // Gets text from file for options
                optionButton[index] = new JRadioButton(optionButtonText[index]);
                optionButton[index].setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
                optionButton[index].setBounds(50, 200+(index*80), 900, 60); // Adjusted size and position
                this.add(optionButton[index]);
                optionButtonGroup.add(optionButton[index]);
                optionButton[index].setEnabled(false); //change this when actually using it
            }

            this.add(timerLabel);
            this.add(scoreLabel);
            this.add(submitButton);
            this.add(countdownLabel);
            this.add(pollButton);
        }
        else
        {
            timerLabel = null;
            clock = null;
            isAnswering = false;
            //this.question = question;
            this.getContentPane().removeAll();
            this.revalidate();
            this.repaint();

            this.setSize(1200, 800); // Increased window size
            this.setLocationRelativeTo(null); // Center the window
            this.setLayout(null);
            this.setVisible(true);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setResizable(false);

            // JLabel questionLabel = new JLabel(question);
            // this.add(questionLabel, BorderLayout.CENTER);
            // this.revalidate();
            // this.repaint();
            this.questionFile = questionFile;
            timerLabel = new JLabel("Timer");
            timerLabel.setBounds(400, 20, 200, 50); // Adjusted size and position
            timerLabel.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
            timerLabel.setForeground(Color.BLUE); // Changed text color

            clock = new CustomTimerTask(15, this);
            Timer timer = new Timer();
            timer.schedule(clock, 0, 1000);

            scoreLabel = new JLabel("Score: " + scoreCount);
            scoreLabel.setBounds(50, 20, 200, 50);
            scoreLabel.setFont(new Font("Arial", Font.PLAIN, 24));

            countdownLabel = new JLabel("Time: ");
            countdownLabel.setBounds(300, 20, 200, 50); // Adjusted size and position
            countdownLabel.setFont(new Font("Arial", Font.PLAIN, 24));

            this.question = new JLabel(question);
            this.question.setFont(new Font("Times New Roman", Font.BOLD, 32));
            this.question.setBounds(50, 50, 1000, 100);
            this.add(this.question);

            pollButton = new JButton("Poll");
            pollButton.setBounds(50, 600, 200, 50); // Increased size
            pollButton.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size

            pollButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    client.sendPoll();
                }
            });
            
            // Initialize Submit Button
            submitButton = new JButton("Submit");
            submitButton.setBounds(300, 600, 200, 50);
            submitButton.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
            submitButton.setEnabled(false);
            //submitButton.addActionListener(this);

            optionButton = new JRadioButton[4];
            optionButtonText = new String[4];
            optionButtonGroup = new ButtonGroup();
            for(int index=0; index<optionButton.length; index++)
            {
                String possibleAnswer = String.valueOf(questionFile[index].charAt(0));// Gets text from file for question
                optionButtonText[index] = questionFile[index]; // Gets text from file for options
                optionButton[index] = new JRadioButton(optionButtonText[index]);
                optionButton[index].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        client.sendAnswer(possibleAnswer);
                    }
                });
                optionButton[index].setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
                optionButton[index].setBounds(50, 200+(index*80), 900, 60); // Adjusted size and position
                this.add(optionButton[index]);
                optionButtonGroup.add(optionButton[index]);
                optionButton[index].setEnabled(false); //change this when actually using it
            }

            this.add(timerLabel);
            this.add(scoreLabel);
            this.add(submitButton);
            this.add(countdownLabel);
            this.add(pollButton);
        }
    }

    private void answerCorrect(String message)
    {
        System.out.print("Calling answer correct method");
        this.getContentPane().removeAll();
        this.revalidate();
        this.repaint();

        this.scoreCount = this.scoreCount + 10;
        JLabel correctLabel = new JLabel(message);
        correctLabel.setBounds(500, 50, 1000, 100);
        this.add(correctLabel);
        this.repaint();
        this.revalidate();
    }

    private void noPoll(String message)
    {
        this.getContentPane().removeAll();
        this.revalidate();
        this.repaint();

        JLabel noPoll = new JLabel(message);
		int screenWidth = 1200;
        int screenHeight = 800;
        int labelWidth = 200;
        int labelHeight = 50;

        int x = (screenWidth - labelWidth) / 2;
        int y = (screenHeight - labelHeight) / 2;

        noPoll.setBounds(x, y, labelWidth, labelHeight);
		noPoll.setFont(new Font("Arial", Font.PLAIN, 16)); 
		noPoll.setForeground(Color.BLUE); 

        this.add(noPoll);
        
    }

    private void noAnswer()
    {
        //this.question = question;
        this.getContentPane().removeAll();
        this.revalidate();
        this.repaint();

        this.setSize(1200, 800); // Increased window size
        this.setLocationRelativeTo(null); // Center the window
        this.setLayout(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        
        this.scoreCount = this.scoreCount - 20;
        JLabel noAnswer = new JLabel("No answer was submitted!");
        noAnswer.setFont(new Font("Times New Roman", Font.BOLD, 32));
        noAnswer.setBounds(400, 200, 1000, 100);
        this.add(noAnswer);

        this.revalidate();
        this.repaint();
    }

    private void nextQuestion(String message)
    {
        this.getContentPane().removeAll();
        this.revalidate();
        this.repaint();

        this.setSize(1200, 800); // Increased window size
        this.setLocationRelativeTo(null); // Center the window
        this.setLayout(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        
        JLabel noAnswer = new JLabel("You didnt win the poll.\nGet ready for the next question!");
        noAnswer.setFont(new Font("Times New Roman", Font.BOLD, 32));
        noAnswer.setBounds(400, 200, 1000, 100);
        this.add(noAnswer);

        this.revalidate();
        this.repaint();
    }
    private void answerIncorrect(String message)
    {
        this.getContentPane().removeAll();
        this.revalidate();
        this.repaint();

        this.setSize(1200, 800); // Increased window size
        this.setLocationRelativeTo(null); // Center the window
        this.setLayout(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        
        this.scoreCount = this.scoreCount - 20;
        JLabel noAnswer = new JLabel("You got it wrong! -10 points.");
        noAnswer.setFont(new Font("Times New Roman", Font.BOLD, 32));
        noAnswer.setBounds(400, 200, 1000, 100);
        this.add(noAnswer);

        this.revalidate();
        this.repaint();
    }
    public static class CustomTimerTask extends TimerTask {
		private int duration;  // write setters and getters as you need
		private boolean blink = false;
		private Color transparentRed = new Color(255, 0, 0, 100); // More transparent red color
		private int elapsedTime = 0; // New variable to store elapsed time
        private AppWindow window;
        private boolean isAnswerSubmitted = false;


		public CustomTimerTask(int duration, AppWindow window) {
			this.duration = duration;
            this.window = window;
		}

        public void onAnswerSubmitted() {
            isAnswerSubmitted = true;
            // rest of your answer submission logic
        }

        public void resetAnswerSubmitted() {
            isAnswerSubmitted = false;
        }

		public int getElapsedTime() {
			return elapsedTime;
		}
		
		@Override
		public void run() {
			elapsedTime++; // Increment elapsed time each second

            if(duration < 0 && !isAnswerSubmitted && isAnswering) {
                timerLabel.setText("Time Expired!");
                window.repaint();
                pollButton.setEnabled(false);
                submitButton.setEnabled(false);
                for (JRadioButton option : optionButton) {
                    option.setEnabled(false);
                }
                window.getContentPane().setBackground(null); // Reset background color
                this.cancel();  // cancel the timed task
                window.getClient().sendAnswer("No answer");
                System.out.println("The client never decided to answer");
                return;
            }
			if (duration < 0) {
				timerLabel.setText("Time Expired!");
				window.repaint();
				pollButton.setEnabled(false);
				submitButton.setEnabled(false);
				for (JRadioButton option : optionButton) {
					option.setEnabled(false);
				}
				window.getContentPane().setBackground(null); // Reset background color
				this.cancel();  // cancel the timed task
                window.getClient().sendTimeout();
				return;
			}

			if (duration < 6) {
				timerLabel.setForeground(transparentRed); // Use transparent red color
				if (blink) {
					window.getContentPane().setBackground(transparentRed); // Blink with transparent red
				} else {
					window.getContentPane().setBackground(null); // Revert to original color
				}
				blink = !blink; // Toggle blink state
			} else {
				timerLabel.setForeground(Color.BLACK);
			}

			timerLabel.setText(String.valueOf(duration));
			duration--;
			window.repaint();
		}
	}
}
