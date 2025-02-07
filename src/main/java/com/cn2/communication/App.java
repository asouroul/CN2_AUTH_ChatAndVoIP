package com.cn2.communication;

import java.io.*;
import java.net.*;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color;
import java.lang.Thread;

public class App extends Frame implements WindowListener, ActionListener {

	/*
	 * Definition of the app's fields
	 */
	static TextField inputTextField;		
	static JTextArea textArea;				 
	static JFrame frame;					
	static JButton sendButton;				
	static JTextField meesageTextField;		  
	public static Color gray;				
	final static String newline="\n";		
	static JButton callButton;				
	
	// TODO: Please define and initialize your variables here...	    
	 static DatagramSocket socketAudio;
	 static DatagramSocket socket;    
	 public static void initSocket() {
	    try {
	        socket = new DatagramSocket(30000);	       
	    } catch (SocketException e) {
	        e.printStackTrace(); // 
	    }
	}   	     
	 public static void initSocketAudio() {
		    try {
		        socketAudio = new DatagramSocket(30002);	       
		    } catch (SocketException e) {
		        e.printStackTrace(); // 
		    } 
		}   
	 
	 static byte[] ReceiveBuffer = new byte[1024];
	 static byte[] SendBuffer = new byte[1024];
	 static byte[] AudioBuffer = new byte[1024];
	 static byte[] ReceiveAudioBuffer = new byte[1024];
	  /**
	 * Construct the app's frame and initialize important parameters
	 */
	public App(String title) {
		
		/*
		 * 1. Defining the components of the GUI
		 */
		
		// Setting up the characteristics of the frame
		super(title);									
		gray = new Color(254, 254, 254);		
		setBackground(gray);
		setLayout(new FlowLayout());			
		addWindowListener(this);	
		
		// Setting up the TextField and the TextArea
		inputTextField = new TextField();
		inputTextField.setColumns(20);
		
		// Setting up the TextArea.
		textArea = new JTextArea(10,40);			
		textArea.setLineWrap(true);				
		textArea.setEditable(false);			
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		//Setting up the buttons
		sendButton = new JButton("Send");			
		callButton = new JButton("Call");			
						
		/*
		 * 2. Adding the components to the GUI
		 */
		add(scrollPane);								
		add(inputTextField);
		add(sendButton);
		add(callButton);
		
		/*
		 * 3. Linking the buttons to the ActionListener
		 */
		sendButton.addActionListener(this);			
		callButton.addActionListener(this);	

		
	}
	static class listenerReceive implements Runnable {	 
		public void run() {
			DatagramPacket receivePacket = new DatagramPacket(ReceiveBuffer, ReceiveBuffer.length);
		do{		
	// TODO: Your code goes here...		 									 																									 
				 try {
						socket.receive(receivePacket);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			 String message = new String(ReceiveBuffer, 0, ReceiveBuffer.length);
			 textArea.append("remote:" + message + "\n");							  			   		
			 for (int i = 0; i < ReceiveBuffer.length; i++) {
				    ReceiveBuffer[i] = 0;                         // Empty Buffer
				}
		      }while(true);
		   }
		}
	
	static class ListenForAudio implements Runnable {
		@Override
	   public void run() {
			DatagramPacket AudioPacketReceive = new DatagramPacket(ReceiveAudioBuffer, ReceiveAudioBuffer.length);
		do {	
			try {
				socketAudio.receive(AudioPacketReceive);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   playAudio(ReceiveAudioBuffer, ReceiveAudioBuffer.length);
		  }while(true);
		} 
		private void playAudio(byte[] receiveAudioBuffer, int length) {
			length = receiveAudioBuffer.length;
			AudioFormat formatAudio = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000, 8, 1, 1, 8000, false);
			DataLine.Info infoSound = new DataLine.Info(SourceDataLine.class, formatAudio);
		
		   SourceDataLine speakers;
		try {
			speakers = (SourceDataLine) AudioSystem.getLine(infoSound);
			speakers.open(formatAudio);
			speakers.start();
		    speakers.write(receiveAudioBuffer, 0, length);
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		   
		 }
	}
	/**
	 * The main method of the application. It continuously listens for
	 * new messages.
	 */

	   	
	 public static void main(String[] args){
		 
		initSocket();  // Initialising the socket
		initSocketAudio(); 
		/*
		 * 1. Create the app's window
		 */
		App app = new App("CN2 - AUTH");  // TODO: You can add the title that will displayed on the Window of the App here																		  
		app.setSize(500,250);				  
		app.setVisible(true);				  
        
        /*
		 * 2. 
		 */
				
		listenerReceive listening = new listenerReceive();
		Thread MyThread = new Thread(listening);
		MyThread.start();
		 
		 
	    ListenForAudio AudioListen = new ListenForAudio();
		Thread Listen = new Thread(AudioListen);
		Listen.start();
			
			 
	 }
			 /**
	 * The method that corresponds to the Action Listener. Whenever an action is performed
	 * (i.e., one of the buttons is clicked) this method is executed. 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
	

		/*
		 * Check which button was clicked.
		 */
		if (e.getSource() == sendButton){
			
			// The "Send" button was clicked
			
			// TODO: Your code goes here...
		
			String message_in = inputTextField.getText();
			byte[] SendBuffer = message_in.getBytes();
			int recipientPort = socket.getLocalPort();
			String recipientIP = "192.168.10.18";
			InetAddress recipientAddress = null;				
			try {
				recipientAddress = InetAddress.getByName(recipientIP);
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}																	    
		DatagramPacket Sendpacket = new DatagramPacket(SendBuffer, SendBuffer.length, recipientAddress,recipientPort);			   
				try {
					socket.send(Sendpacket);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}								
			    textArea.append("local:" + message_in + "\n");
			    inputTextField.setText("");
			    for (int i = 0; i < SendBuffer.length; i++) {
			        SendBuffer[i] = 0;                               //Empty buffer
			    }
		}else if(e.getSource() == callButton){
			
			// The "Call" button was clicked
			
			// TODO: Your code goes here...
			
			AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000, 8, 1, 1, 8000, false);				 
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);	
			TargetDataLine mic = null;
			try { 
			   mic = (TargetDataLine) AudioSystem.getLine(info);			 
		       mic.open(format);
		       mic.start();
			     } catch (LineUnavailableException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			    }
			int recipientPortAudio = socketAudio.getLocalPort();
			String recipientIPAudio = "192.168.10.18";
			InetAddress recipientAddressAudio = null;				
			try {
				recipientAddressAudio = InetAddress.getByName(recipientIPAudio);
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}					
			while (true) {
				mic.read(AudioBuffer,0 ,AudioBuffer.length);
				DatagramPacket AudioPacketSend = new DatagramPacket(AudioBuffer, AudioBuffer.length, recipientAddressAudio, recipientPortAudio);
				try {
					socketAudio.send(AudioPacketSend);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			 }
		
		}
			

	}

	/**
	 * These methods have to do with the GUI. You can use them if you wish to define
	 * what the program should do in specific scenarios (e.g., when closing the 
	 * window).
	 */
	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		dispose();
        	System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub	
	}
}
