package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JTextField;

import ui.ClientGUI;

public class ClientController {
	private final static String XTER_PREFIX = "xTer: ";
	private final static String HANNAH_PREFIX = "Hannah: ";
	private final static String PORT_REGEX = "[0-9]+";
	
	private static ServerSocket serverSocket;
	private static Socket socket;
	private static ObjectOutputStream outputStream;
	private static ObjectInputStream inputStream;
	private static boolean isStreamsSetup;

	public static void startRunning(JTextField serverAddress, JTextField serverPort) {
		String server = serverAddress.getText();
		String port = serverPort.getText();
		
		try{
			int portNumber;
			if (server != null && port != null && port.matches(PORT_REGEX)){
				portNumber = Integer.parseInt(port);
				if(portNumber > 1024 && portNumber <= 65535){
					initConnection(server, portNumber);
				} else {
					ClientGUI.showMessage("Your port number must be between 1024 and 65535");
				}
			} else {
				ClientGUI.showMessage("Invalid server/port");
			}
			
			while(true){
				try {
					setupStreams();
					chattingInstance();
				} catch (EOFException eof) {
					ClientGUI.showMessage("Server ended the connection!");
				} finally {
					closeConnection();
				}
				
			}
		} catch (IOException e){
			ClientGUI.showMessage(e.getMessage());
		}
	}
	
	/** 
	 * Initialize a connection 
	 */
    private static void initConnection(String server, int port) throws IOException {
	    	socket = new Socket(server,port);
	    	ClientGUI.showMessage("\nNow connected to " + socket.getInetAddress().getHostName());
    }
    
	/** 
	 * Wait for connection 
	 */
    static void waitForConnection() throws IOException {
	    	ClientGUI.showMessage("Waiting for someone to connect... ");
	    	socket = serverSocket.accept();
	    	ClientGUI.showMessage("Now connected to " + socket.getInetAddress().getHostName());
    }

    /**
     * Set up streams
     */
    static void setupStreams() throws IOException {
	    	// Create output stream from socket
	    	outputStream = new ObjectOutputStream(socket.getOutputStream());
	    	outputStream.flush();
	    	
	    	// Create input stream from socket
	    	inputStream = new ObjectInputStream(socket.getInputStream());

	    	isStreamsSetup = true;
    }
    
    /**
     * Maintain chatting's instance
     */
    static void chattingInstance() throws IOException {
	    	String message = "You are now connected! ";
	    	sendMessage(message);
	    	do {
	    		try {
	    			message = (String) inputStream.readObject();
	    			ClientGUI.showMessage(message);
	    		} catch (EOFException e) {
	    			ClientGUI.showMessage("Disconnected to server!");
	    			break;
	    		} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
	    	} while(!message.equalsIgnoreCase("End"));
    }
    
    static void sendMessage(String message) throws IOException {
    		if (isStreamsSetup) {
	    		// Write the message object to output stream
	    		outputStream.writeObject(message);
	    		// Flush output stream
	    		outputStream.flush();
	    		// Show message
	    		ClientGUI.showMessage("Me: " + message);
    		} else {
    			ClientGUI.showMessage("You are not connected to server!");
    		}
    }
    /**
     * Method to close the connection
     */
    public static void closeConnection() {
        try {
	        	outputStream.close();
	        	inputStream.close();
            socket.close();
            isStreamsSetup = false;
        } catch (IOException e) {
        		e.printStackTrace();
        }
    }
    
    public static class EventContainer {
    		
    		/**
    		 * An action listener method to listen for enter key be pressed
    		 * 
    		 * @param textField The text field where user inputs the message
    		 * @return The ActionListener of pressing enter event
    		 */
    		public ActionListener pressEnterToSend(JTextField textField) {
    			return new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							sendMessage(e.getActionCommand());
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
						textField.setText("");
					}
				};
    		}
    		
    		/**
    		 * An action listener method to listen for send button be clicked
    		 * 
    		 * @param textField The text field where user inputs the message
    		 * @return The ActionListener of clicking Listen button event
    		 */
    		public ActionListener clickToSend(JTextField textField) {
    			return new ActionListener() {
    				@Override
    				public void actionPerformed(ActionEvent e) {
    					String message = textField.getText();
    					try {
    						sendMessage(message);
    					} catch (IOException ioe) {
    						ioe.printStackTrace();
    					}
    					textField.setText("");
    				}
    			};
    		}
    		
    		public ActionListener clickToConnect(JTextField serverAddress, JTextField serverPort) {
    			return new ActionListener() {
    				@Override
    				public void actionPerformed(ActionEvent e) {
    					new Thread(){
    						public void run(){
    							startRunning(serverAddress, serverPort);
    						}
    					}.start();
    				}
    			};
    		}
    	
    }
}
