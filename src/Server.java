import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {

	private static final int NUMBER_OF_ROOMS = 1;
	private static final int MAX_CLIENTS_PER_ROOM = 1;
	
	private static int numberOfClients;
	
	public static List<Room> rooms;

	public static void main(String[] args) throws IOException {
		InitServer();
		ServerFunctionality ();
	}
	
	public static void InitServer () {
		rooms = new ArrayList<Room>();
	}

	public static void ServerFunctionality () throws IOException {
		String clientMessage;
		int serverResponse;
		
		ServerSocket welcomeSocket = new ServerSocket(6789);
		
		while(true) {
			//Accepts connect, creates new ClientThread to handle connection
			Socket conSocket = welcomeSocket.accept(); //New Connection
			ClientThread x = new ClientThread(conSocket, numberOfClients++);
			x.start();
		}
	}
	
	public static void AddRoom (String roomName) {
		System.out.println("Creating chatroom: " + roomName);
		Room newRoom = new Room(roomName, 8);
		rooms.add(newRoom);
	}
	
	public static void AddClientToRoom (ClientThread client) {
		
		
	}
}
