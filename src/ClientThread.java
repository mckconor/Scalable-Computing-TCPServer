import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThread extends Thread {

	protected Socket socket;
	protected InputStream input;
	protected BufferedReader bufferedReader;
	protected DataOutputStream output;
	
	public ClientThread(Socket socket) {
		this.socket = socket;
	}
	
	public void run() { 
		input = null;
		bufferedReader = null;
		output = null;
		
		try {
			input = socket.getInputStream();
			bufferedReader = new BufferedReader(new InputStreamReader(input));
			output = new DataOutputStream(socket.getOutputStream());
		} catch (IOException ex) {
			return;
		}
		
		String line;
		
		//Welcome Client
		WelcomeClient();
		
		while(true) {
			try {
				line = bufferedReader.readLine();
				if((line == null) || line.equalsIgnoreCase("QUIT")) {
					//Closing client
					socket.close();
					return;
				} else {
					output.writeBytes(line + "\n\r");
					System.out.println("FROM CLIENT: " + line);
					output.flush();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				return;
			}
		}
		
		
	}
	
	public void WelcomeClient () {
		try {
			output.writeBytes("Welcome!" + "\n\r");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
