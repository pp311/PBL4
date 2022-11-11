package Server.pl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;

import Server.bll.UploadBLL;
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
	private ServerSocket dataServer;
	public String userName = "";
	private String password = "";
	private String workingDir = "/";
	public String baseDir = "/home/shared";
	static final int MAX = 7;
	private String response;
    private Vector messages = new Vector();
	
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
			System.out.println(userName + ": " + ch);
			//những lệnh như PWD ko chứa dấu " " nên gọi substring sẽ lỗi
			if(ch.startsWith("PWD") || ch.startsWith("PASV")) cmd = ch;
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
				//ServerSocket server;
				int port = 0;
				//chon port ngau nhien
				while(true) {
					 port = generator.nextInt((PASV_PORT_END - PASV_PORT_START) + 1) + PASV_PORT_START;
					try {
						dataServer = new ServerSocket(port);
//						dataConnection = new DataConnectionHandler(dataServer, this);
//						dataConnection.start();
						break;
					} catch (IOException e) {
						//nếu đã có kết nối ở port dc chon thì sẽ có lôĩ
						//catch ở đây để vòng lặp while dc tiếp tuc lặp
					}	
				}
				this.dos.writeUTF("227 Entering Passive Mode (" + port + ")");
				Socket clientSoc = dataServer.accept();
				dataConnection = new DataConnectionHandler(clientSoc, this);
				dataConnection.start();
				break;
			case "PORT":
				String ip = msg.substring(1, msg.indexOf("|"));
				port = Integer.valueOf(msg.substring(msg.indexOf("|")+1, msg.indexOf(")")));
				Socket clientActiveSoc = new Socket(ip, port);
				dataConnection = new DataConnectionHandler(clientActiveSoc, this);
				dataConnection.start();
				this.dos.writeUTF("227 Server connected to " + ip + "/" + port);
				break;	
			case "LIST":
				putMessage("LIST " + msg);
				dataConnection.join();
				response = dataConnection.getResponseMessage();
				dos.writeUTF(response);
				break;
			case "STOR":
				putMessage("STOR " + msg);
				dataConnection.join();
				response = dataConnection.getResponseMessage();
				dos.writeUTF(response);
				break;
			case "RETR":
				putMessage("RETR " + msg);
				dataConnection.join();
				response = dataConnection.getResponseMessage();
				dos.writeUTF(response);
				break;
			case "DELE":
				File deleteDir = new File(baseDir + msg);
				deleteDirectory(deleteDir);
				dos.writeUTF("250 Delete operation successful");
				break;
			case "CWD":
				if(!msg.equals(".."))
					workingDir = msg;
				else 
					workingDir = workingDir.substring(0, workingDir.lastIndexOf("/", workingDir.length() - 2)+1);
				dos.writeUTF("250 Directory successfully changed");
				break;
			case "PWD":
				this.dos.writeUTF("PWD " + workingDir);
				break;
			case "MKD":
				File newDir = new File(baseDir + File.separator + msg);
                boolean success = newDir.mkdir();
//                if (success) {
//                	JOptionPane.showMessageDialog(null, "Successfully created directory: " + name);
//                } else {
//                	JOptionPane.showMessageDialog(null, "Failed to create directory. See server's reply.");		                 
//                }
				//this.dos.writeUTF("MKD "+ workingDir);
                if(success) {
                	dos.writeUTF("257 \"/" + msg + "\" created");
                }
                else {
                	dos.writeUTF("502 Command not implemented");
                }
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

private boolean deleteDirectory(File directoryToBeDeleted) {
    File[] allContents = directoryToBeDeleted.listFiles();
    if (allContents != null) {
        for (File file : allContents) {
            deleteDirectory(file);
        }
    }
    return directoryToBeDeleted.delete();
}

private synchronized void putMessage(String cmd)
        throws InterruptedException
    {
        // checks whether the queue is full or not
        while (messages.size() == MAX)
            // waits for the queue to get empty
            wait();
        // then again adds element or messages
        messages.addElement(cmd);
        notify();
    }
  
    public synchronized String getMessage()
        throws InterruptedException
    {
        notify();
        while (messages.size() == 0)
            wait();
        String message = (String)messages.firstElement();
  
        // extracts the message from the queue
        messages.removeElement(message);
        return message;
    }
}