import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {

	private static final int NUMBER_OF_ROOMS = 1;
	private static final int MAX_CLIENTS_PER_ROOM = 1;
	
	static List<Room> rooms;

	public static void main(String[] args) throws IOException {
		InitServer();
		ServerFunctionality ();
	}
	
	public static void InitServer () {
		rooms = new ArrayList<Room>();
		for(int i = 0; i<NUMBER_OF_ROOMS; i++){
			Room newRoom = new Room(i, MAX_CLIENTS_PER_ROOM);
			rooms.add(newRoom);
		}
	}

	public static void ServerFunctionality () throws IOException {
		String clientMessage;
		int serverResponse;
		
		ServerSocket welcomeSocket = new ServerSocket(6789);
		
		while(true) {
			Socket conSocket = welcomeSocket.accept();
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(conSocket.getInputStream()));			
			DataOutputStream outToClient = new DataOutputStream(conSocket.getOutputStream());
			
			outToClient.writeBytes("Welcome! Please enter a room number to join: ");
			
			clientMessage= inFromClient.readLine();
			
			if(clientMessage.equals("1")){
				serverResponse = 1;
				outToClient.write(serverResponse);
			}
			
		}
	}
}
