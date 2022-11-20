package Server.pl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	ServerSocket serverSoc;
	int port = 2100;
	public Server() {
		TFTPListenHandler tftpServer = new TFTPListenHandler();
		tftpServer.start();
		try {
			serverSoc = new ServerSocket(port);
			listenClientConnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void listenClientConnect() {
		while(true) {
			try {
				Socket clientSoc = serverSoc.accept();
				ListenHandler lh = new ListenHandler(clientSoc);
				lh.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args) {
		new Server();
	}

}