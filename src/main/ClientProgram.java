package main;

import javax.swing.JFrame;

import ui.ClientGUI;

public class ClientProgram {

	public static void main(String[] args) {
		ClientGUI client = new ClientGUI("Messenger Client");
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.createAndShowGUI();
	}
}
