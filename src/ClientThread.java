import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientThread extends Thread {

	protected Socket socket;
	protected InputStream input;
	protected BufferedReader bufferedReader;
	protected DataOutputStream output;
	protected OutputStreamWriter outputWriter;
	protected BufferedWriter bufferedWriter;

	// Chat Functions
	private static final String JOIN_ROOM = "join_chatroom";
	private static final String LEAVE_ROOM = "leave_chatroom";
	private static final String CHANGE_NAME = "change_name";
	private static final String CHAT = "chat";
	private static final String KILL_SERVER = "kill_service";
	private static final String WELCOME = "helo";
	private static final String DISCONNECT = "disconnect";

	private int clientNumber;
	public String clientName;

	// Room stuff
	public String joinId, roomId;

	public ClientThread(Socket socket, int clientNumber) {
		this.socket = socket;
		this.clientNumber = clientNumber;

		clientName = "client" + this.clientNumber;
	}

	public void run() {
		input = null;
		bufferedReader = null;
		output = null;

		try {
			input = socket.getInputStream();
			bufferedReader = new BufferedReader(new InputStreamReader(input));
			output = new DataOutputStream(socket.getOutputStream());
			outputWriter = new OutputStreamWriter(socket.getOutputStream());
			bufferedWriter = new BufferedWriter(outputWriter);
		} catch (IOException ex) {
			return;
		}

		String line;

		// Accepts commands
		while (true) {
			try {
				line = ReadInput(); 
				System.out.println("Received from " + this.clientName + ":\n" + line);
				
				if ((line == null) || line.toLowerCase().contains(DISCONNECT.toLowerCase())) {
					//Kill Client
					DisconnectClient();
					return;
				} else if (line.toLowerCase().contains(JOIN_ROOM.toLowerCase())) {
					// Joining a room
					JoinRoom(line);
				} else if (line.toLowerCase().contains(LEAVE_ROOM.toLowerCase())) {
					// Leave a room
					LeaveRoom(line);
				} else if (line.toLowerCase().contains(KILL_SERVER.toLowerCase())) {
					// Kill program
					Server.Shutdown();
				} else if (line.toLowerCase().contains(CHAT.toLowerCase())) {
					// Allow Chat
					Server.ChatMessageToAllInRoom(this, CleanUpCharacters(':', ParseMessageComponent("MESSAGE", line)+"\n"), CleanUpCharacters(':', ParseMessageComponent("CHAT", line)));
				} else if (line.toLowerCase().contains(WELCOME.toLowerCase())) {
					// Responds to HELO message
					WelcomeClient(line);
				} else if (line.toLowerCase().contains(CHANGE_NAME.toLowerCase())) {
					// Changing client name
					ChangeClientName(line);
				} else if (line.equals("") || line.equals("\n")) {
					System.out.println("ERROR_CODE: 124\nERROR_DESCRIPTION: Received empty input");
					bufferedWriter.write("ERROR_CODE: 124\nERROR_DESCRIPTION: Received empty input");
				} else {
					System.out.println("ERROR_CODE: 123\nERROR_DESCRIPTION: Received other input");
					bufferedWriter.write("ERROR_CODE: 123\nERROR_DESCRIPTION: Received other input");
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				return;
			}
		}
	}
	
	//Read in from client
	public String ReadInput () {
		InputStreamReader inputReader = new InputStreamReader(this.input);
		char[] buffer = new char[4096];
		char[] result = null;
		int read = 0;
		
		try {
			read = inputReader.read(buffer, 0, buffer.length);
			if(read > 0) {
				result = new char[read];
				System.arraycopy(buffer, 0, result, 0, read);
				return new String(result);
			} else {
				return null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	//Parse Message Component
	public String ParseMessageComponent (String component, String line) {
		if(!line.toLowerCase().contains(component.toLowerCase())) {
			return null;
		}
		
		String messageComponent = "";
		int componentLocation = line.toLowerCase().indexOf(component.toLowerCase());
		String restOfString = line.substring(componentLocation, line.length());
		int nextBreakLine = restOfString.indexOf("\n");
		if(nextBreakLine < 0){
			messageComponent = restOfString.substring(component.length()+1, restOfString.length());
		} else {
			messageComponent = restOfString.substring(component.length()+1, nextBreakLine);
		}
		
		return messageComponent;
	}
	
	//Clean up String
	public String CleanUpCharacters (char character, String line) {
		StringBuilder sb = new StringBuilder();
		sb.append(line);
		try {
			sb.deleteCharAt(line.indexOf(character));
		} catch (Exception ex) {}
		
		return sb.toString();
	}

	// Welcome Client on join
	public void WelcomeClient(String line) {
		String response = WELCOME.toUpperCase() + " " + ParseMessageComponent(WELCOME, line)
				+ "\n" + "IP:" + Server.serverIp 
				+ "\n" + "Port:" + Server.serverPort 
				+ "\n" + "StudentID:" + Server.studentNumber + "\n";

		try {
			bufferedWriter.write(response);
			bufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Joins client to room if exists, else creates it
	public void JoinRoom(String line) throws IOException {
		String roomName = CleanUpCharacters(':', ParseMessageComponent(JOIN_ROOM, line)).trim(); //line.substring(JOIN_ROOM.length(), line.length()).trim();
		String clientNameIn = CleanUpCharacters(':', ParseMessageComponent("CLIENT_NAME", line)).trim();
		this.clientName = clientNameIn;
		
		System.out.println(line + "\nCLIENT_IP:" + socket.getInetAddress() + "\nPORT:" + socket.getPort()
				+ "\nCLIENT_NAME" + clientName);

		if (roomName == null || roomName.equals("")) {
			System.err.println("ERROR: Client " + clientName + " tried to join a null room.");
			output.writeBytes("Cannot join a null chatroom, please enter a room name to join or create." + "\n");
		} else {

			roomId = "";
			boolean roomExists = false;
			for (Room x : Server.rooms) {
				if (x.roomName.equals(roomName)) {
					// Join
					roomExists = true;
					roomId = x.roomId;
				}
			}

			if (!roomExists) {
				// Create and Join
				System.out.println("Chatroom " + roomName + " does not exist. Creating..." + "\n");

				String portNumber = CleanUpCharacters(':', ParseMessageComponent("PORT", line)); //"" + Server.serverPort + Server.rooms.size() + 1;
				Room room = Server.AddRoom(roomName, portNumber);
				roomId = room.roomId;
				joinId = Server.AddClientToRoom(this, roomName);

				String response = "JOINED_CHATROOM:" + roomName 
						+ "\n" + "SERVER_IP: " + Server.serverIp 
						+ "\n" + "PORT: " + Server.serverPort 
						+ "\n" + "ROOM_REF: " + roomId 
						+ "\n" + "JOIN_ID: " + joinId + "\n";

				//Server out
				System.out.println("-Room created- \n" + response);
				
				bufferedWriter.write(response);
				bufferedWriter.flush();
				
				//Join message
				Server.ChatMessageToAllInRoom(this, this.clientName + " has joined the chatroom.\n", roomId);
			} else {
				joinId = Server.AddClientToRoom(this, roomName);

				String response = "JOINED_CHATROOM:" + roomName 
						+ "\n" + "SERVER_IP: " + Server.serverIp 
						+ "\n" + "PORT: " + Server.serverPort 
						+ "\n" + "ROOM_REF: " + roomId 
						+ "\n" + "JOIN_ID: " + joinId + "\n";

				//Server out
				System.out.println("-Room exists- \n" + response);
				
				bufferedWriter.write(response);
				bufferedWriter.flush();

				//Join message
				Server.ChatMessageToAllInRoom(this, this.clientName + " has joined the chatroom.\n", roomId);
			}
		}

	}

	// Allows client to leave room
	public void LeaveRoom(String line) throws IOException {
		String roomRef = CleanUpCharacters(':', ParseMessageComponent(LEAVE_ROOM, line));
		String response = "LEFT_CHATROOM:" + roomRef + "\n" + "JOIN_ID:" + joinId + "\n";

		bufferedWriter.write(response);
		bufferedWriter.flush();

		System.out.println("-Leave Room- \n" + response);
		
		Server.RemoveClientFromRoom(this, roomRef);
	}

	// Allows client rename themselves --DEPRECATED
	public void ChangeClientName(String line) throws IOException {
		String nameChange = line.substring(CHANGE_NAME.length(), line.length()).trim();

		if (nameChange == null || nameChange.equals("")) {
			System.err.println("ERROR: Client " + clientName + " tried to change their name to null.");
			output.writeBytes("Cannot change name to nothing, please enter a valid name." + "\n");
		} else {
			System.out.println("Client " + clientName + " has changed their name to " + nameChange);
			this.clientName = nameChange;
			output.writeBytes("Successfully updated name to " + clientName + "\n");
		}
	}

	// Kills client connection
	public void DisconnectClient() throws IOException {
		Server.DisconnectClient(this);
		
		bufferedWriter.flush();
		bufferedReader.close();
		output.flush();
		output.close();
		outputWriter.flush();
		outputWriter.close();
		input.close();
		
		socket.close();
		System.out.println(this.clientName + "client Disconnected");
	}
}
