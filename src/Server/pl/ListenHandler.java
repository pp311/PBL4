package Server.pl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import Server.bll.UserBLL;

class ListenHandler extends Thread {
	private static final int PASV_PORT_START = 25000;
	private static final int PASV_PORT_END = 30000;
	private DataConnectionHandler dataConnection;
	public Socket incoming;
	public DataInputStream dis;
	public DataOutputStream dos;
	public Socket soc;
	public Server server;
	private String userName = "";
	private String password = "";
	private String workingDir = "/";
public ListenHandler(Socket i) {
	this.soc=i;
	try{
		this.dis= new DataInputStream(soc.getInputStream());
		this.dos= new DataOutputStream(soc.getOutputStream());
		}catch(IOException e){
			
		}
	}
public void run(){
	String ch = "";
	String cmd = "";
	String msg = "";
	while(true) {
		try {
			ch= dis.readUTF();
			System.out.println(ch);
			if(ch.startsWith("PWD")) cmd = ch;
			else {
				cmd=ch.substring(0, ch.indexOf(" "));
				msg=ch.substring(ch.indexOf(" ")+1);
				cmd = cmd.toUpperCase();				
			}
			switch (cmd) {
			case "LGIN":
				
				userName = msg.substring(0,msg.indexOf(" "));
				password = msg.substring(msg.indexOf(" ") + 1);
				if(new UserBLL().checkLoginInfo(userName, password)) {
					this.dos.writeUTF("230 User logged in, proceed.");
				}
				else {
					this.dos.writeUTF("530 Not logged in.");
				}
				break;
			case "PASV":
				Random generator = new Random();
				ServerSocket server;
				int port = 0;
				//chon port ngau nhien
				while(true) {
					 port = generator.nextInt((PASV_PORT_END - PASV_PORT_START) + 1) + PASV_PORT_START;
					try {
						server = new ServerSocket(port);
						dataConnection = new DataConnectionHandler(server, ch);
						break;
					} catch (IOException e) {
						//nếu đã có kết nối ở port dc chon thì sẽ có lôĩ
						//catch ở đây để vòng lặp while dc tiếp tuc lặp
					}	
				}
				this.dos.writeUTF("PORT " + port);
				break;
			case "PWD":
				this.dos.writeUTF(workingDir);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + cmd );
			}
		} catch (EOFException e) {
			//EOFExcep xảy ra khi client thoát, server chạy lệnh dos.readUTF() -> lỗi
			try {
				soc.close();
				System.out.println(userName + " da ngat ket noi");
				break;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	
}
}
