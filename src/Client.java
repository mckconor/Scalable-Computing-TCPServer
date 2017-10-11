import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
	public static void main(String argv[]) throws Exception {
		  String sentence;
		  String modifiedSentence;
		  
		  boolean hasRoom = false;
		  
		  while(true){
			  BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			  Socket clientSocket = new Socket("localhost", 6789);
			  DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			  
			  if(!hasRoom){
				  System.out.println(inFromServer.readLine());
				  sentence = inFromUser.readLine();
				  outToServer.writeBytes(sentence + '\n');
				  
				  int serverResponse = inFromServer.read();
				  if(serverResponse == 1){
					  hasRoom = true;
				  }
			  } else {
				  sentence = inFromUser.readLine();
				  outToServer.writeBytes(sentence + '\n');
				  modifiedSentence = inFromServer.readLine();
				  System.out.println("FROM SERVER: " + modifiedSentence);
			  }
		  }
	}
}
