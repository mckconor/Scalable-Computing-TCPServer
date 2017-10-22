
public class Message {

	public String clientIp, clientPort, clientName;
	
	public String serverIp, serverPort, serverName, joinId, roomId;
	
	//Client Message
	public Message (String clientIp, String clientPort, String clientName) {
		this.clientIp = clientIp;
		this.clientPort = clientPort;
		this.clientName = clientName;
	}
	
	//Server message
	public Message (String serverIp, String serverPort, String serverName, String joinId, String roomId) {
		this.serverIp = serverIp;
		this.serverPort = serverPort;
		this.serverName= serverName;
		this.joinId = joinId;
		this.roomId= roomId;
	}

}
