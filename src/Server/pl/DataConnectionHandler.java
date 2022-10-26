package Server.pl;

import java.net.ServerSocket;
import java.net.Socket;

public class DataConnectionHandler extends Thread{
	private ServerSocket soc;
	public DataConnectionHandler(ServerSocket soc, String command) {
		this.soc = soc;
	}
	
}
