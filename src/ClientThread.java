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
	
	private int messageCount;
	
	//Room stuff
	public int joinId;
	
	//TCD
	private String studentNumber = "13320488";
	
	//Chat Functions
	private static final String JOIN_ROOM = "join_chatroom";
	private static final String LEAVE_ROOM = "leave_chatroom";
	private static final String CHANGE_NAME = "change_name";	//redundant, but fun!
	private static final String CHAT = "chat";
	private static final String KILL_SERVER = "kill_service";
	private static final String WELCOME = "helo";
	private static final String DISCONNECT = "disconnect";
	
	public ClientThread(Socket socket, int clientNumber) {
		this.socket = socket;
		this.clientNumber = clientNumber;
		
		clientName = "Client#" + this.clientNumber;
		
		messageCount = 0;
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
		
		//Accepts commands
		while(true) {
			try {
				line = bufferedReader.readLine();
				
				if((line == null) || line.toLowerCase().contains(DISCONNECT.toLowerCase())) {
					//Closing client
					socket.close();
					return;
				} else if (line.toLowerCase().contains(JOIN_ROOM.toLowerCase())) {
					//Joining a room
					JoinRoom(line);
				} else if (line.toLowerCase().contains(LEAVE_ROOM.toLowerCase())) {
					//Leave a room
//					LeaveRoom();
				} else if (line.toLowerCase().contains(KILL_SERVER.toLowerCase())) {
					//Kill program
					
				} else if (line.toLowerCase().contains(CHAT.toLowerCase())) {
					//Allow Chat

				} else if (line.toLowerCase().contains(WELCOME.toLowerCase())) {
					//Responds to HELO message
					WelcomeClient(line);
				} else if (line.toLowerCase().contains(CHANGE_NAME.toLowerCase())) { 
					//Changing client name
					ChangeClientName(line);
				} else if (line.equals("") || line.equals("\n")) { 
					//Do nothing
				} else {
					output.writeBytes(clientName + ": " + line + "\n");
					System.out.println("FROM " + clientName+ ": " + line);
					output.flush();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				return;
			}
		}
	}
	
	//Welcome Client on join
	public void WelcomeClient (String line) {
		String response = line + "\n"
				+ "IP:" + Server.serverIp + "\n"
				+ "Port:" + Server.serverPort + "\n"
				+ "StudentID:" + studentNumber + "\n";
		
		try {
			output.writeBytes(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Joins client to room if exists, else creates it
	public void JoinRoom (String line) throws IOException {
		String roomName = line.substring(JOIN_ROOM.length(), line.length()).trim();
		
		Message message = new Message (socket.getInetAddress().toString(), ""+socket.getPort(), this.clientName);
		System.out.println(line + "\nCLIENT_IP:" + message.clientIp + "\nPORT:" + message.clientPort + "\nCLIENT_NAME" + message.clientName);
		
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
				System.out.println("Chatroom " + roomName + " does not exist. Creating..." + "\n");
				
				int portNumber = Server.serverPort + Server.rooms.size() + 1;
				Server.AddRoom(roomName, portNumber);
				Message serverResponse = Server.AddClientToRoom(this, roomName);
				
				String response = "JOINED_CHATROOM:" +roomName + "\n"
						+ "SERVER_IP:" + serverResponse.serverIp + "\n"
						+ "PORT:" + serverResponse.serverPort + "\n"
						+ "ROOM_REF:" + serverResponse.roomId + "\n"
						+ "JOIN_ID:" + serverResponse.joinId + "\n";
				
				output.writeBytes(response);
			} else {
				Message serverResponse = Server.AddClientToRoom(this, roomName);
				
				String response = "JOINED_CHATROOM:" +roomName + "\n"
						+ "SERVER_IP:" + serverResponse.serverIp + "\n"
						+ "PORT:" + serverResponse.serverPort + "\n"
						+ "ROOM_REF:" + serverResponse.roomId + "\n"
						+ "JOIN_ID:" + serverResponse.joinId + "\n";
				
				output.writeBytes(response);
			}
		}
	
	}
	
	//Allows client rename themselves
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
