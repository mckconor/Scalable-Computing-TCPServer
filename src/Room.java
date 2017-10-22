import java.util.ArrayList;
import java.util.List;

public class Room {

	String roomName;
	int maxClients;
	int roomId;
	
	String roomIpAddress;
	int roomPort;
	
	List<ClientThread> clients;
		
	public Room (String roomName, int roomId, int roomPort) {
		this.roomName = roomName;
		this.roomPort = roomPort;
		this.roomId = roomId;
		
		clients = new ArrayList<ClientThread>();
	}
	
//	public boolean addClient (ClientThread client) {
//		if(clients.size() < this.maxClients){
//			clients.add(client);
//			return true;
//		} else {
//			return false;
//		}
//	}
	
	public void addClient(ClientThread client) {
		clients.add(client);
	}
	
	public void removeClient (ClientThread client) {
		clients.remove(client);
	}
}
