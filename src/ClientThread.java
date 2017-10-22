import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThread extends Thread {

	protected Socket socket;
	protected InputStream input;
	protected BufferedReader bufferedReader;
	protected DataOutputStream output;
	
	private int clientNumber;
	private String clientName;
	
	//Chat Functions
	private static final String JOIN_ROOM = "join_chatroom:";
	private static final String CHANGE_NAME = "change_name_to:";
	
	public ClientThread(Socket socket, int clientNumber) {
		this.socket = socket;
		this.clientNumber = clientNumber;
		
		clientName = "#" + this.clientNumber;
	}
	
	public void run() { 
		input = null;
		bufferedReader = null;
		output = null;
		
		try {
			input = socket.getInputStream();
			bufferedReader = new BufferedReader(new InputStreamReader(input));
			output = new DataOutputStream(socket.getOutputStream());
		} catch (IOException ex) {
			return;
		}
		
		String line;
		
		//Welcome Client
		WelcomeClient();
		
		while(true) {
			try {
				line = bufferedReader.readLine();
				if((line == null) || line.equalsIgnoreCase("QUIT")) {
					//Closing client
					socket.close();
					return;
				} else if (line.toLowerCase().contains(JOIN_ROOM.toLowerCase())) {
					JoinClient(line);
				} else if (line.toLowerCase().contains(CHANGE_NAME.toLowerCase())) { 
					ChangeClientName(line);
				} else {
					output.writeBytes(line + "\n");
					System.out.println("FROM CLIENT " + clientName+ ": " + line);
					output.flush();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				return;
			}
		}
	}
	
	public void WelcomeClient () {
		try {
			output.writeBytes("Welcome! You are Client #" + clientNumber + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void JoinClient (String line) throws IOException {
		String roomName = line.substring(JOIN_ROOM.length(), line.length()).trim();
		
		if(roomName == null || roomName.equals("")) {
			System.err.println("ERROR: Client " + clientName + " tried to join a null room.");
			output.writeBytes("Cannot join a null chatroom, please enter a room name to join or create." + "\n");
		} else {
			
			boolean roomExists = false;
			for(Room x : Server.rooms) {
				if(x.roomName.equals(roomName)) {
					//Join
					roomExists = true;
				}
			}
			
			if(!roomExists) {
				//Create and Join
				output.writeBytes("Chatroom " + roomName + " does not exist. Creating..." + "\n");
				
				Server.AddRoom(roomName);
				Server.AddClientToRoom(this);
			}
		}
	
	}
	
	public void ChangeClientName (String line) throws IOException {
		String nameChange = line.substring(CHANGE_NAME.length(), line.length()).trim();
		
		if(nameChange == null || nameChange.equals("")) {
			System.err.println("ERROR: Client " + clientName + " tried to change their name to null.");
			output.writeBytes("Cannot change name to nothing, please enter a valid name." + "\n");
		} else {
			System.out.println("Client " + clientName + " has changed their name to " + nameChange);
			this.clientName = nameChange;
			output.writeBytes("Successfully updated name to " + clientName + "\n");
		}
	}
}
