import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientListener extends Thread{

	BufferedReader input;
	
	public ClientListener () {
		input = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public void run() {
		while(true) {
			
		}
	}
	
}
