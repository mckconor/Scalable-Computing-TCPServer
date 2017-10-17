import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThread extends Thread {

	Socket socket;
	
	public ClientThread(Socket socket) {
		this.socket = socket;
	}
	
	public void run() { 
		
		InputStream input = null;
		BufferedReader bufferedReader = null;
		DataOutputStream output = null;
		
		try {
			input = socket.getInputStream();
			bufferedReader = new BufferedReader(new InputStreamReader(input));
			output = new DataOutputStream(socket.getOutputStream());
		} catch (IOException ex) {
			return;
		}
		
		String line;
		
		while(true) {
			try {
				line = bufferedReader.readLine();
				if((line == null) || line.equalsIgnoreCase("QUIT")) {
					//Closing client
					socket.close();
					return;
				} else {
					output.writeBytes(line + "\n\r");
					output.flush();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				return;
			}
		}
		
		
	}
	
}
