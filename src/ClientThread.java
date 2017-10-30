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
	private static final String CHANGE_NAME = "change_name"; // redundant, but fun!
	private static final String CHAT = "chat";
	private static final String KILL_SERVER = "kill_service";
	private static final String WELCOME = "helo";
	private static final String DISCONNECT = "disconnect";

	private int clientNumber;
	public String clientName;

	// Room stuff
	public int joinId, roomId;
	private boolean isInRoom;

	public ClientThread(Socket socket, int clientNumber) {
		this.socket = socket;
		this.clientNumber = clientNumber;

		clientName = "Client#" + this.clientNumber;
		isInRoom = false;
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
				line = bufferedReader.readLine();

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

				} else if (line.toLowerCase().contains(CHAT.toLowerCase())) {
					// Allow Chat
					while(!line.contains("MESSAGE")) {
						line = bufferedReader.readLine();
					}
					ChatWithRoom(line);
				} else if (line.toLowerCase().contains(WELCOME.toLowerCase())) {
					// Responds to HELO message
					WelcomeClient(line);
				} else if (line.toLowerCase().contains(CHANGE_NAME.toLowerCase())) {
					// Changing client name
					ChangeClientName(line);
				} else if (line.equals("") || line.equals("\n")) {
					// Do nothing
				} else {
					System.out.println("FROM " + clientName + ": " + line);
					output.flush();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				return;
			}
		}
	}
	
	//Parse Message
	public String ParseMessage (String line) {
		String messageFlag = "MESSAGE";
		if(!line.contains(messageFlag)) {
			//Get outta here
			return null;
		}
		
		int messageLocation = line.indexOf(messageFlag);
		String restOfString = line.substring(messageLocation, line.length());
		int nextBreakLine = restOfString.indexOf("\\n\\n");
		String messageLine = restOfString.substring(messageFlag.length()+1, nextBreakLine);
		
		return messageLine;
	}

	// Welcome Client on join
	public void WelcomeClient(String line) {
		String response = line + "\n" + "IP:" + Server.serverIp + "\n" + "Port:" + Server.serverPort + "\n"
				+ "StudentID:" + Server.studentNumber + "\n";

		try {
			bufferedWriter.write(response);
			bufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Joins client to room if exists, else creates it
	public void JoinRoom(String line) throws IOException {
		String roomName = line.substring(JOIN_ROOM.length(), line.length()).trim();

		System.out.println(line + "\nCLIENT_IP:" + socket.getInetAddress() + "\nPORT:" + socket.getPort()
				+ "\nCLIENT_NAME" + clientName);

		if (roomName == null || roomName.equals("")) {
			System.err.println("ERROR: Client " + clientName + " tried to join a null room.");
			output.writeBytes("Cannot join a null chatroom, please enter a room name to join or create." + "\n");
		} else if (isInRoom) {
			output.writeBytes("Cannot join room when you're already in one! Please leave and try again.");
		} else {

			roomId = 0;
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

				int portNumber = Server.serverPort + Server.rooms.size() + 1;
				Room room = Server.AddRoom(roomName, portNumber);
				roomId = room.roomId;
				joinId = Server.AddClientToRoom(this, roomName);

				String response = "JOINED_CHATROOM:" + roomName + "\n" + "SERVER_IP:" + Server.serverIp + "\n" + "PORT:"
						+ Server.serverPort + "\n" + "ROOM_REF:" + roomId + "\n" + "JOIN_ID:" + joinId + "\n";

				bufferedWriter.write(response);
				bufferedWriter.flush();
				isInRoom = true;
			} else {
				joinId = Server.AddClientToRoom(this, roomName);

				String response = "JOINED_CHATROOM:" + roomName + "\n" + "SERVER_IP:" + Server.serverIp + "\n" + "PORT:"
						+ Server.serverPort + "\n" + "ROOM_REF:" + roomId + "\n" + "JOIN_ID:" + joinId + "\n";

				bufferedWriter.write(response);
				bufferedWriter.flush();
			}
		}

	}

	// Allows client to leave room
	public void LeaveRoom(String line) throws IOException {
		String roomName = Server.RemoveClientFromRoom(this);

		String response = "LEFT_CHATROOM:" + roomName + "\n" + "JOIN_ID:" + joinId + "\n";

		bufferedWriter.write(response);
		bufferedWriter.flush();

		isInRoom = false;
	}

	// Allows client rename themselves
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
		bufferedWriter.flush();
		bufferedReader.close();
		output.flush();
		output.close();
		outputWriter.flush();
		outputWriter.close();
		input.close();
		
		socket.close();
		Server.KillClient(this);
		System.out.println("clientDisconnected");
	}
	
	public void ChatWithRoom (String line) throws IOException {
		String message = ParseMessage(line);
		Server.MessageToAllInRoom(this, message);
	}
}
