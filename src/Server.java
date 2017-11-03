import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	
	public static void InitServer (String[] args) throws UnknownHostException {
		System.out.println("Server running...");
		
		serverIp = InetAddress.getLocalHost().getHostAddress(); //args[0];
		serverPort = Integer.parseInt(args[1]);
		
		serverName = serverIp + ":" + serverPort;
		
		rooms = new ArrayList<Room>();
		allClients = new ArrayList<ClientThread>();
		
		alive = true;
	}
	
	static boolean alive;
	static ServerSocket serverSocket;
	public static void ServerFunctionality () throws IOException {
		serverSocket = new ServerSocket(serverPort);
		
		while(true) {
			//Accepts connect, creates new ClientThread to handle connection
			Socket conSocket = serverSocket.accept(); //New Connection
			ClientThread x = new ClientThread(conSocket, numberOfClients++);
			allClients.add(x);
			x.start();
		}
	}
	
	public static void Shutdown () throws IOException {
		alive = false;
		
		for(ClientThread x : allClients) {
			x.socket.close();
		}
		
		for(Room x: rooms){
			for(ClientThread y: x.clients){
				y.socket.close();
			}
		}
		serverSocket.close();
		System.exit(0);
	}
	
	public static Room AddRoom (String roomName, String serverPort) {
		System.out.println("Creating chatroom: " + roomName);
		Room newRoom = new Room(roomName, ""+rooms.size()+1, serverPort);
		rooms.add(newRoom);
		return newRoom;
	}
	
	public static String AddClientToRoom (ClientThread client, String roomName) throws IOException {
		//Find room and add to it
		String joinId = ""; String roomId = "";
		for(Room room : rooms) {
			if(room.roomName.equals(roomName)) {
				client.joinId = "" + room.clients.size()+1;
				room.addClient(client);
				joinId = "" + room.clients.size();
				roomId = room.roomId;
				
				//Print join message
				for(ClientThread x : room.clients) {
				}
			}
		}
		return joinId;
	}
	
	public static String RemoveClientFromRoom (ClientThread client, String roomRef) throws IOException {
		String roomName = "";
		for(Room room: rooms) {
			if(room.roomId.equals(roomRef.trim())) {
				ChatMessageToAllInRoom(client, client.clientName + " has left the chatroom.\n", room.roomId);
				roomName = room.roomName;
				room.clients.remove(client);
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
	
	public static void BroadcastToAllInRoom (ClientThread client, String message) throws IOException {
		List<Room> clientRooms = new ArrayList<Room>();
		
		for(Room x : rooms) {
			if(x.roomId == client.roomId) {
				clientRooms.add(x);
			}
		}

		for(Room chatRoom : clientRooms){
			for(ClientThread x : chatRoom.clients) {
				x.bufferedWriter.write(message);
				x.bufferedWriter.flush();
			}
		}
	}
	
	public static void BroadcastToAllOthersInRoom (ClientThread client, String message) throws IOException {
		//For not broadcasting to itself
		List<Room> clientRooms = new ArrayList<Room>();
		
		for(Room x : rooms) {
			if(x.roomId == client.roomId) {
				clientRooms.add(x);
			}
		}
		
		for(Room chatRoom : clientRooms){
			for(ClientThread x : chatRoom.clients) {
				if(!client.equals(x)) {
					x.bufferedWriter.write(message);
					x.bufferedWriter.flush();
				}
			}
		}
	}
	
	public static void ChatMessageToAllInRoom (ClientThread client, String message, String roomRef) throws IOException {
		Room chatRoom = null;
		for(Room x : rooms) {
			if(x.roomId.equals(roomRef.trim())) {
				chatRoom = x;
			}
		}
		
		String fullMessage = "CHAT: " + chatRoom.roomId + "\n" + 
				"CLIENT_NAME: " + client.clientName + "\n" + 
				"MESSAGE: " + message + "\n";
		
		System.out.println(fullMessage);
		
		for(ClientThread x : chatRoom.clients) {
			x.bufferedWriter.write(fullMessage);
			x.bufferedWriter.flush();
		}
	}
	
	public static void DisconnectClient(ClientThread client) throws IOException {
		List<Room> clientRooms = new ArrayList<Room>();
		for(Room x : rooms) {
			if(x.clients.contains(client)) {
				clientRooms.add(x);
			}
		}

		Collections.sort(clientRooms, Server.Comparators.ref);
		for(Room x : rooms){
			if(x.clients.contains(client)) {
				RemoveClientFromRoom(client, x.roomId);
			}
		}
	}
	
	public static class Comparators {
		public static Comparator<Room> ref = new Comparator<Room>(){
			@Override
			public int compare(Room r1, Room r2){
				return r2.roomId.compareTo(r1.roomId);
			}
		};
	}
}
