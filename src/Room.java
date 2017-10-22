import java.util.List;

public class Room {

	String roomName;
	int maxClients;
	int roomId;
	
	String roomIpAddress;
	int roomPort;
	
	List<Client> clients;
		
	public Room (String roomName, int maxClients) {
		this.roomName = roomName;
		this.maxClients = maxClients;
	}
	
	public boolean addClient (Client client) {
		if(clients.size() < this.maxClients){
			clients.add(client);
			return true;
		} else {
			return false;
		}
	}
	
	public void removeClient (Client client) {
		clients.remove(client);
	}
}
