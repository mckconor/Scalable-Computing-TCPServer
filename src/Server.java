import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

public class Server {
	
	public static String serverIp;
	public static int serverPort;
	private static String serverName;
	
	private static int numberOfClients;
	public static List<ClientThread> allClients;
	public static List<Room> rooms;
	
	//TCD
	public static String studentNumber = "13320488";

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
	
	public static Room AddRoom (String roomName, int serverPort) {
		System.out.println("Creating chatroom: " + roomName);
		Room newRoom = new Room(roomName, rooms.size()+1, serverPort);
		rooms.add(newRoom);
		return newRoom;
	}
	
	public static int AddClientToRoom (ClientThread client, String roomName) throws IOException {
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
//					x.output.writeBytes(client.clientName + " has joined the chatroom: " + room.roomName + "\n");
				}
			}
		}
		return joinId;
	}
	
	public static String RemoveClientFromRoom (ClientThread client) throws IOException {
		String roomName = "";
		for(Room room: rooms) {
			if(room.clients.contains(client)) {
				roomName = room.roomName;
				room.clients.remove(client);
				
				//Print leave message to remaining clients
				for(ClientThread x : room.clients) {
					x.output.writeBytes(client.clientName + " has left the chatroom: " + room.roomName + "\n");
				}
			}
		}
		return roomName;
	}
	
	public static void KillClient (ClientThread client) {
		for(Room x : rooms) {
			if(x.roomId == client.roomId) {
				x.clients.remove(client);
			}
		}
		allClients.remove(client);
	}
	
	public static void MessageToAllInRoom (ClientThread client, String message) throws IOException {
		Room chatRoom = null;
		for(Room x : rooms) {
			if(x.roomId == client.roomId) {
				chatRoom = x;
			}
		}
		
		String fullMessage = " CHAT: " + chatRoom.roomId + "\r\n" + 
				"CLIENT_NAME: " + client.clientName + "\r\n" + 
				"MESSAGE: " + message;
		
		for(ClientThread x : chatRoom.clients) {
			x.output.writeBytes(fullMessage);
		}
	}
}
