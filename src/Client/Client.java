package Client;

import javax.swing.*;

import Client.Client.CustomTimerTask;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.util.TimerTask;
import java.util.Timer;
import java.util.UUID;

public class Client implements ActionListener
{
	private JButton pollButton;
	private JButton submitButton;
	private JRadioButton optionButton[];
	private String optionButtonText[]; //To store text for each option
	private ButtonGroup optionButtonGroup;
	private int currentSelection;
	private JLabel question;
	private JLabel timerLabel;
	private JLabel countdownLabel;
	private JLabel scoreLabel;
    private int scoreCount;
	private TimerTask clock;

	// clientID is given from the server
	private Integer ClientID;
	private String questionNumber;
	private Socket socket;
	private ObjectInputStream inputStream;
	private DataOutputStream outputStream;
	private boolean isAllowedToSelect = true; // Flag to track if the client is allowed to select options


	private JFrame window;
	
	private static SecureRandom random = new SecureRandom();
	
	public Client(String ipAddress, int port) throws FileNotFoundException
	{   
		//IP
		
		// Initialize JFrame
		window = new JFrame("LETS PLAY TRIVIA!");
		window.setSize(1200, 800); // Increased window size
		window.setLocationRelativeTo(null); // Center the window
		window.setLayout(null);
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);

		// Initialize Timer
		timerLabel = new JLabel("Timer");
		timerLabel.setBounds(400, 20, 200, 50); // Adjusted size and position
		timerLabel.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
		timerLabel.setForeground(Color.BLUE); // Changed text color

		clock = new CustomTimerTask(30);
		Timer timer = new Timer();
		timer.schedule(clock, 0, 1000);

		// Initialize Score Label
		scoreLabel = new JLabel("Score: " + scoreCount);
		scoreLabel.setBounds(50, 20, 200, 50);
		scoreLabel.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size

		// Initialize Countdown Label
		countdownLabel = new JLabel("Time: ");
		countdownLabel.setBounds(300, 20, 200, 50); // Adjusted size and position
		countdownLabel.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size

		// Initialize Poll Button
		pollButton = new JButton("Poll");
		pollButton.setBounds(50, 600, 300, 100); // Increased size
		pollButton.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
		pollButton.addActionListener(this);

		// Initialize Submit Button
		submitButton = new JButton("Submit");
		submitButton.setBounds(400, 600, 300, 100); // Increased size
		submitButton.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
		submitButton.addActionListener(this);

		// Add components to the JFrame
		window.add(timerLabel);
		window.add(scoreLabel);
		window.add(countdownLabel);
		window.add(pollButton);
		window.add(submitButton);

		// Call method to connect to server
		serverConnect(ipAddress, port);
	}


	public void showWindow() {
        window.setVisible(true);
    }

	// connect to server with argument of IP address
	public void serverConnect(String ipAddress, int port){
		try	{
			// create a socket connection to the server
			// the thread immediately sends the client its ID and the client saves it
			socket = new Socket(ipAddress, port);
			inputStream = new ObjectInputStream(socket.getInputStream());
			outputStream = new DataOutputStream(socket.getOutputStream());
			this.ClientID = Integer.valueOf(inputStream.readInt());
			System.out.println("Hello i am client " + this.ClientID);
		} 
		catch(IOException ioException){
			ioException.printStackTrace();
		}

		SwingWorker<Void, Void> worker = new SwingWorker<Void,Void>() {
			@Override
			protected Void doInBackground() {
				try{
					while (true){
						// read message type from server and take different action depending on it
							String messageType = (String) inputStream.readObject();
							if (messageType.equals("File".trim())){
								// first thing that is sent is the question number
								int questionNum = inputStream.readInt();
								if (questionNum < 10){
									questionNumber = "0" + questionNum;
								}
								else {
									questionNumber = "" + questionNum;
								}
								// call process to start up a new question and display it
								String[] questionInfo = new String[5];
								for (int i = 0; i < 5; i++) {
									questionInfo[i] = (String) inputStream.readObject();
								}
								displayQuestion(questionInfo);
							}
							// if the message is a score message, then increment and display score
							else if (messageType.equals("Score".trim())) {
								scoreCount += inputStream.readInt();
								scoreLabel.setText("SCORE: " + scoreCount);
							}
							else if (messageType.equals("ack")) {
                            handleAcknowledgment("ack");
                        	} 
							else if (messageType.equals("negative-ack")) {
                            handleAcknowledgment("negative-ack");
                        	}
					}
				}
				catch (IOException e){
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return null;
				
			}
		};
		worker.execute();
		
	}

	private void handleAcknowledgment(String acknowledgmentType) {
        if (acknowledgmentType.equals("ack")) {
            isAllowedToSelect = true; // Set flag to true if acknowledgment is "ack"
            // Enable options and submit button
            pollButton.setEnabled(true);
            submitButton.setEnabled(true);
            for (JRadioButton option : optionButton) {
                option.setEnabled(true);
            }
        } else if (acknowledgmentType.equals("negative-ack")) {
            // Disable options and submit button
            isAllowedToSelect = false;
            pollButton.setEnabled(false);
            submitButton.setEnabled(false);
            for (JRadioButton option : optionButton) {
                option.setEnabled(false);
            }
            JOptionPane.showMessageDialog(window, "You were late in polling. Options disabled.");
        }
    }

	public void displayQuestion(String[] questionFile){

		// Remove existing components from the window
		window.getContentPane().removeAll();
	
		// Add the new question label
		question = new JLabel(questionFile[0]);
		question.setFont(new Font("Times New Roman", Font.BOLD, 32)); // Increased font size
		window.add(question);
		question.setBounds(50, 50, 1000, 100); // Adjusted position
	
		// Add new radio buttons for options
		optionButton = new JRadioButton[4];
		optionButtonText = new String[4];
		optionButtonGroup = new ButtonGroup();
		for(int index=0; index<optionButton.length; index++)
		{
			optionButtonText[index] = questionFile[index+1]; // Gets text from file for options
			optionButton[index] = new JRadioButton(optionButtonText[index]);
			optionButton[index].addActionListener(this);
			optionButton[index].setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
			optionButton[index].setBounds(50, 200+(index*80), 900, 60); // Adjusted size and position
			window.add(optionButton[index]);
			optionButtonGroup.add(optionButton[index]);
			optionButton[index].setEnabled(false);
		}
	
		// Add the buttons
		window.add(pollButton);
		window.add(submitButton);
	
		// Add the timer labels
		window.add(timerLabel);
		window.add(countdownLabel);
	
		// Add the score
		window.add(scoreLabel);
	
		// Repaint the window to reflect changes
		window.revalidate();
		window.repaint();
	}
	

	@Override
	public void actionPerformed(ActionEvent action)
	{
		String input = action.getActionCommand();  
		
		if(input.equals("Poll")){
			if (isAllowedToSelect) {
				// Notify the server that the "Poll" button is pressed
				try {
					outputStream.writeBoolean(true); // Send a boolean indicating the "Poll" button is pressed
					outputStream.flush();
				} catch (IOException exception3) {
					exception3.printStackTrace();
				}

				pollButton.setEnabled(false);
				submitButton.setEnabled(true);
				for (JRadioButton option : optionButton) {
					option.setEnabled(true);
				}
				byte[] data = null;
				String message = ClientID + "," + questionNumber;
				data = message.getBytes();
				InetAddress ip = null;
				try {
					ip = InetAddress.getLocalHost();
				} catch (UnknownHostException exception) {
					exception.printStackTrace();
				}
				int port = 1234;
				DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
				DatagramSocket socket = null;
				try {
					socket = new DatagramSocket();
					socket.send(packet);
				} catch (IOException exception1) {
					exception1.printStackTrace();
				}
			}
			else
			{
				JOptionPane.showMessageDialog(window, "Waiting for server acknowledgment...");
			}
		} else if(input.equals("Submit")){
			if (isAllowedToSelect) {
                try {
                    outputStream.writeInt(currentSelection);
                    outputStream.flush();
                } catch (IOException exception2) {
                    exception2.printStackTrace();
                }

                pollButton.setEnabled(true);
                submitButton.setEnabled(false);
                for (JRadioButton option : optionButton) {
                    option.setEnabled(false);
                }
            } else 
			{
                JOptionPane.showMessageDialog(window, "Waiting for server acknowledgment...");
            }
		} else if(input.equals(optionButtonText[0])){
			currentSelection = 0;
		} else if(input.equals(optionButtonText[1])){
			currentSelection = 1;
		} else if(input.equals(optionButtonText[2])){
			currentSelection = 2;
		} else if(input.equals(optionButtonText[3])){
			currentSelection = 3;
		} else{
			System.out.println("Incorrect Option");
		}	
	}
	
	public class CustomTimerTask extends TimerTask {
		private int duration;  // write setters and getters as you need
		private boolean blink = false;
		private Color transparentRed = new Color(255, 0, 0, 100); // More transparent red color

		public CustomTimerTask(int duration) {
			this.duration = duration;
		}

		@Override
		public void run() {
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