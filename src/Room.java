import java.util.List;

public class Room {

	int roomId;
	int maxClients;
	
	List<Client> clients;
		
	public Room (int roomId, int maxClients) {
		this.roomId = roomId;
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
