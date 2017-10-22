import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client{
	public static void main(String argv[]) throws Exception {
		  String sentence;
		  String modifiedSentence;
		  
		  Socket clientSocket = new Socket("localhost", 1234);

		  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		  
		  ServerListener serverListener = new ServerListener(inFromServer);
		  serverListener.start();
		  
		  ClientListener clientListener = new ClientListener(clientSocket);
		  clientListener.start();
		  
		  while(true){
		  }
	}
}
