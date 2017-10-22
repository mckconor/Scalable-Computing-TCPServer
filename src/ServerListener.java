import java.io.BufferedReader;
import java.io.IOException;

public class ServerListener extends Thread{

	BufferedReader serverReader;
	
	public ServerListener (BufferedReader inFromServer) {
		serverReader = inFromServer;
	}
	
	public void run() {
		while(true) {
			//Print
			try {
				System.out.println(serverReader.readLine());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			//Polling
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
