package Server.pl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import Server.bll.FileBLL;
import Server.bll.UploadBLL;
import Server.bll.UserBLL;
import Common.dto.UserDto;
import Common.dto.FileDto;

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
	public int UID = 0;
	public String userName = "";
	private String password = "";
	public String role = "";
	private String workingDir = "/";
	public volatile boolean isConneted = false;
//	public String baseDir = "D:\\Tai xuong\\ftp";
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
	try {
	while(true) {	
			ch= dis.readUTF();
			
			//những lệnh như PWD ko chứa dấu " " nên gọi substring sẽ lỗi
			if(ch.startsWith("PWD") || ch.startsWith("PASV") || ch.startsWith("QUIT")) cmd = ch;
			else {
				cmd=ch.substring(0, ch.indexOf(" "));
				msg=ch.substring(ch.indexOf(" ")+1);
				cmd = cmd.toUpperCase();				
			}
			
			FileWriter fw = new FileWriter("log.txt", true);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
			if(!cmd.equals("LGIN"))
				fw.append(simpleDateFormat.format(new Date()) + " : " + userName + " : " + ch + "\n");
			else
				fw.append(simpleDateFormat.format(new Date()) + " : " + userName + " : " + ch.substring(0, ch.lastIndexOf(" ")) + " ***" + "\n");
			fw.close();
			System.out.println(userName + " : " + ch);
			
			switch (cmd) {
			case "LGIN":
				userName = msg.substring(0,msg.indexOf(" "));
				password = msg.substring(msg.indexOf(" ") + 1);
				if(new UserBLL().checkLoginInfo(userName, password)) {
					UserDto u = new UserBLL().getCurrentUserInfo(userName);
					role = u.getRole();
					UID = u.getUID();
					isConneted = true;
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
					FileDto fileDto = new FileDto();
					Path pa = Paths.get(deleteDir.getAbsolutePath());
					BasicFileAttributes attri = Files.readAttributes(pa, BasicFileAttributes.class);
					fileDto.setPath(pa.toString());
					fileDto.setName(deleteDir.getName());
					fileDto.setType(attri.isDirectory() ? "Dir" : "File");
					fileDto.setFID(new UploadBLL().findFID(fileDto));
	//				if (fileDto.getType().compareTo("Dir")==0)
	//				{
	//					
	//				}
	//				else {
	//					
	//				}
				if (role.equals("admin"))
				{
					if (new UploadBLL().delFile(fileDto)) {
						deleteDirectory(deleteDir);
						fileDto.setFID( new UploadBLL().findFID(fileDto));
						List<Integer> listFID = new UploadBLL().getAllFID(fileDto);
						for (int i=1;i<listFID.size();i++)
				        {
				        	if (new UploadBLL().uploadFolder(new java.sql.Timestamp(new Date().getTime()), userName,listFID.get(i)))
				        	{
				        		System.out.println("success");
				        	}
				        	else {
				        		System.out.println("fail");
				        	}	
				        }
						dos.writeUTF("250 Delete operation successful");
					}
					else {
						dos.writeUTF("502 Command not implemented");
					}	
				}
				else {
					List<String> list = new UploadBLL().getAllOwner(fileDto);
					int dem=0;
					fileDto.setFID( new UploadBLL().findFID(fileDto));
					List<Integer> listFID = new UploadBLL().getAllFID(fileDto);
					for (int i=0;i<list.size();i++)
					{
						if(list.get(i).equals(userName)) dem++;
					}
					if (dem>0){
						if (new UploadBLL().delFile(fileDto)) {
							deleteDirectory(deleteDir);
							for (int i=1;i<listFID.size();i++)
					        {
					        	if (new UploadBLL().uploadFolder(new java.sql.Timestamp(new Date().getTime()), userName,listFID.get(i)))
					        	{
					        		System.out.println("success");
					        	}
					        	else {
					        		System.out.println("fail");
					        	}	
					        }
							dos.writeUTF("250 Delete operation successful");
						}
						else {
							dos.writeUTF("502 Command not implemented");
						}
					}
					else {
					dos.writeUTF("601 Permission denied");
					}
				}
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
				FileDto fi = new FileDto();
				String temp = baseDir + msg;
				fi.setPath(temp.substring(0,temp.lastIndexOf("/")));
				int permission = new UploadBLL().getPermission(fi);
				if (role.equals("admin") || permission == 2)
				{
					File newDir = new File(baseDir + File.separator + msg);
	                boolean success = newDir.mkdir();
	                FileDto fDto = new FileDto();
	                Path p = Paths.get(newDir.getAbsolutePath());
					BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
					fDto.setName(newDir.getName());
					fDto.setType("Dir");
					fDto.setPath(p.toString());
					fDto.setCreatedDate(new Date(attr.creationTime().toMillis()));
					if (new UploadBLL().checkFileExists(fDto)==false )
					{
						fDto.setOwner(userName);
						fDto.setLastEditedBy(userName);
						fDto.setLastEditedDate(new Date(attr.lastModifiedTime().toMillis()));
						fDto.setSize(attr.size());
						int x= new UploadBLL().parentID(fDto);
					    if (x!=0)
					    {
					    	fDto.setParentID(x);
					    }
					    else {
							fDto.setParentID(0);
						}
						if (new UploadBLL().uploadFile(fDto) && success) {
							dos.writeUTF("257 \"/" + msg + "\" created");
						}
						else {
							dos.writeUTF("502 Command not implemented");
						}
						fDto.setFID( new UploadBLL().findFID(fDto));
						List<Integer> listFID = new UploadBLL().getAllFID(fDto);
						for (int i=1;i<listFID.size();i++)
				        {
				        	if (new UploadBLL().uploadFolder(new java.sql.Timestamp(fDto.getLastEditedDate().getTime()), userName,listFID.get(i)))
				        	{
				        		System.out.println("success");
				        	}
				        	else {
				        		System.out.println("fail");
				        	}	
				        }
					}
					else {
//						fDto.setOwner("");
						dos.writeUTF("602 Directory Existed");
					}	
					
				}
				else {
					File newDir = new File(baseDir + File.separator + msg);
	                boolean success = newDir.mkdir();
	                FileDto fDto = new FileDto();
	                
	                Path p = Paths.get(newDir.getAbsolutePath());
					BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
					fDto.setName(newDir.getName());
					fDto.setType("Dir");
					fDto.setPath(p.toString());
					fDto.setCreatedDate(new Date(attr.creationTime().toMillis()));
					FileDto base = new FileDto();
					
	                base.setPath(fDto.getPath().substring(0, fDto.getPath().lastIndexOf(File.separator)));
	                base.setFID(new UploadBLL().findFID(base));
	                
					List<String> list = new UploadBLL().getAllOwner(base);
			        List<Integer> listFID = new UploadBLL().getAllFID(base);   
			        int dem=0;
			        for (int i=0;i<listFID.size();i++)
			        {
			        	List<String> listSh = new ArrayList<String>();
			        	listSh=new UploadBLL().getAllSharedByFID(listFID.get(i));
			        	for(int j=0;j<listSh.size();j++)
			        	{
			        		if (listSh.get(j).equals(userName)) dem++;
			        	}
			        }
					List<String> listShared = new UploadBLL().getAllShared(base);
					
					for (int i=0;i<list.size();i++)
					{
						if(list.get(i).equals(userName)) dem++;
					}
					for (int i=0;i<listShared.size();i++)
					{
						if(listShared.get(i).equals(userName)) dem++;
					}
					if (dem>0){
						if (new UploadBLL().checkFileExists(fDto)==false )
						{
							fDto.setOwner(userName);
							fDto.setLastEditedBy(userName);
							fDto.setLastEditedDate(new Date(attr.lastModifiedTime().toMillis()));
							fDto.setSize(attr.size());
							int x= new UploadBLL().parentID(fDto);
						    if (x!=0)
						    {
						    	fDto.setParentID(x);
						    }
						    else {
								fDto.setParentID(0);
							}
							if (new UploadBLL().uploadFile(fDto) && success) {
								dos.writeUTF("257 \"/" + msg + "\" created");
							}
							else {
								dos.writeUTF("502 Command not implemented");
							}
							fDto.setFID( new UploadBLL().findFID(fDto));
							List<Integer> listFID2 = new UploadBLL().getAllFID(fDto);
							for (int i=1;i<listFID2.size();i++)
					        {
					        	if (new UploadBLL().uploadFolder(new java.sql.Timestamp(fDto.getLastEditedDate().getTime()), userName,listFID2.get(i)))
					        	{
					        		System.out.println("success");
					        	}
					        	else {
					        		System.out.println("fail");
					        	}	
					        }
						}
						else {
//							fDto.setOwner("");
							dos.writeUTF("602 Directory Existed");
						}	
						
					}
					else {
						dos.writeUTF("601 Permission denied");
					}
				}
//                if (success) {
//                	JOptionPane.showMessageDialog(null, "Successfully created directory: " + name);
//                } else {
//                	JOptionPane.showMessageDialog(null, "Failed to create directory. See server's reply.");		                 
//                }
				//this.dos.writeUTF("MKD "+ workingDir);
//                if(success) {
//                	dos.writeUTF("257 \"/" + msg + "\" created");
//                }
//                else {
//                	dos.writeUTF("502 Command not implemented");
//                }
				break;
			case "LSUSER":
				putMessage("LSUSER " + msg);
				dataConnection.join();
				response = dataConnection.getResponseMessage();
				dos.writeUTF(response);
				break;
			case "LSSHARE":
				putMessage("LSSHARE " + msg);
				dataConnection.join();
				response = dataConnection.getResponseMessage();
				dos.writeUTF(response);
				break;
			case "SHARE":
				putMessage("SHARE " + msg);
				dataConnection.join();
				response = dataConnection.getResponseMessage();
				dos.writeUTF(response);
				break;
			case "SETWR":
				new FileBLL().changePermission(Integer.valueOf(msg), 2);
				dos.writeUTF("Change permission successfully");
				break;
			case "SETRD":
				new FileBLL().changePermission(Integer.valueOf(msg), 1);
				dos.writeUTF("Change permission successfully");
				break;
			case "UINFO":
				putMessage("UINFO " + msg);
				dataConnection.join();
				response = dataConnection.getResponseMessage();
				dos.writeUTF(response);
				break;
			case "ADDUSER":
				putMessage("ADDUSER " + msg);
				dataConnection.join();
				response = dataConnection.getResponseMessage();
				dos.writeUTF(response);
			break;
			case "CHGPASS":
				boolean res = new UserBLL().changePassword(userName, msg);
				if(res) {
					dos.writeUTF("334 Change password successfully");
				} else {
					dos.writeUTF("605 Change password failed");
				}
			break;
			case "QUIT":
				isConneted = false;
				putMessage("TERM");
				//dos.writeUTF("111 Logged out");
			break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + cmd );
			}
		}
	} catch (Exception e) {
		//EOFExcep xảy ra khi client thoát, server chạy lệnh dos.readUTF() -> lỗi
		try {
			isConneted = false;
			putMessage("TERM");
			soc.close();
			System.out.println(userName + " da ngat ket noi");
		} catch (Exception e1) {
			e1.printStackTrace();
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
    
    public synchronized String peakMessage()
            throws InterruptedException
        {
    	String message = "";
    		try {
    			message = (String)messages.firstElement();
			} catch (Exception e) {
				message = "";
			}    
            // extracts the message from the queue
            return message;
        }
    
}