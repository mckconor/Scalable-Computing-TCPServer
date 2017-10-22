import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientListener extends Thread{

	BufferedReader input;
	Socket clientSocket;
	
	public ClientListener (Socket clientSocket) {
		input = new BufferedReader(new InputStreamReader(System.in));
		this.clientSocket = clientSocket;
	}
	
	public void run() {
		while(true) {
			  DataOutputStream outToServer;
			try {
				outToServer = new DataOutputStream(clientSocket.getOutputStream());
				  
				String inputLine = input.readLine();
				outToServer.writeBytes(inputLine + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
