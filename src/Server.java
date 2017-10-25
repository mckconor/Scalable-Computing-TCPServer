import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
	
	public static String serverIp;
	public static int serverPort;

	private static String serverName;
	
	private static int numberOfClients;
	public static List<ClientThread> allClients;
	
	public static List<Room> rooms;

	public static void main(String[] args) throws IOException {
		InitServer(args);
		ServerFunctionality ();
	}
	
	public static void InitServer (String[] args) {
		serverIp = args[0];
		serverPort = Integer.parseInt(args[1]);
		
		serverName = serverIp + ":" + serverPort;
		
		rooms = new ArrayList<Room>();
		allClients = new ArrayList<ClientThread>();
	}

	public static void ServerFunctionality () throws IOException {
		String clientMessage;
		int serverResponse;
		
		ServerSocket serverSocket = new ServerSocket(serverPort);
		
		while(true) {
			//Accepts connect, creates new ClientThread to handle connection
			Socket conSocket = serverSocket.accept(); //New Connection
			ClientThread x = new ClientThread(conSocket, numberOfClients++);
			allClients.add(x);
			x.start();
		}
	}
	
	public static void AddRoom (String roomName, int serverPort) {
		System.out.println("Creating chatroom: " + roomName);
		Room newRoom = new Room(roomName, rooms.size()+1, serverPort);
		rooms.add(newRoom);
	}
	
	public static Message AddClientToRoom (ClientThread client, String roomName) throws IOException {
		//Find room and add to it
		int joinId = 0; int roomId = 0;
		for(Room room : rooms) {
			if(room.roomName.equals(roomName)) {
				client.joinId = room.clients.size()+1;
				room.addClient(client);
				joinId = room.clients.size();
				roomId = room.roomId;
				
				//Print join message
				for(ClientThread x : room.clients) {
					x.output.writeBytes(client.clientName + " has joined the chatroom: " + room.roomName + "\n");
				}
			}
		}
		Message message = new Message (serverIp, ""+serverPort, serverName, ""+joinId, ""+roomId);
		
		return message;
	}
	
	public static Message RemoveClientFromRoom (ClientThread client) throws IOException {
		for(Room room: rooms) {
			if(room.clients.contains(client)) {
				room.clients.remove(client);
				
				//Print join message
				for(ClientThread x : room.clients) {
					x.output.writeBytes(client.clientName + " has left the chatroom: " + room.roomName + "\n");
				}
			}
		}
		Message message = new Message (serverIp, ""+serverPort, serverName, "", "");
		return message;
	}
}
