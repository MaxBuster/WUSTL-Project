package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

public class Client {
	private Socket clientSocket;
	private DataInputStream socketInputStream;
	private DataOutputStream socketOutputStream;

	public Client() {
		try {
			// On PC go to Command Prompt; look for IPv4 after ipconfig
			// On Mac go to Terminal; look for inet after ifconfig |grep inet
			// Ignore 127.0.0.1
			// Lab main laptop: 128.252.177.166
			clientSocket = new Socket("localhost", 10501);
			socketInputStream = new DataInputStream(clientSocket.getInputStream());
			socketOutputStream = new DataOutputStream(clientSocket.getOutputStream());
			ClientHandler handler = new ClientHandler(socketInputStream, socketOutputStream);
			handler.handleIO();
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "Couldn't find server");
			// Throw an error
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "IO Problem");
			// Throw an error
		}
	}

	public static void main(String[] args) {
		Client client = new Client();
	}

}
