package Server.pl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Server.bll.FileBLL;
import Server.bll.ShareBLL;
import Server.bll.UploadBLL;
import Server.bll.UserBLL;
import Server.dto.*;


public class DataConnectionHandler extends Thread{
	//private ServerSocket soc;
	private Socket clientSoc;
	private ListenHandler producer;
	private static final int MAX_BUFFER = 8192;
	private DataInputStream dis;
	private DataOutputStream dos;
	private byte[] buffer;
	private int read;
	private volatile String response = "";
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	public DataConnectionHandler(Socket clientSoc, ListenHandler producer) {
		this.clientSoc = clientSoc;
		this.producer = producer;
	}
	
	public void run() {
		try {
				//Socket clientSoc = soc.accept();
				String message = producer.getMessage();
	            String cmd = message.substring(0, message.indexOf(" "));
	            String params = message.substring(message.indexOf(" ")+1);
	            String baseDir = producer.baseDir; 
	            switch (cmd) {
	            case "UINFO":
	            	oos = new ObjectOutputStream(clientSoc.getOutputStream());
					UserDto userinfo = new UserBLL().getCurrentUserInfo(params);
					oos.writeObject(userinfo);
					oos.close();
					response = "226 User info send OK";
				break;
	            case "ADDUSER":
	            	ois = new ObjectInputStream(clientSoc.getInputStream());
	            	UserDto u = (UserDto)ois.readObject();
	            	boolean res = new UserBLL().createAccount(u);
	            	if(res) {
	            		response = "333 Create account successfully";
	            	}
	            	else {
	            		response = "603 Create account failed";
	            	}
	            break;
	            case "LSUSER":
	            	oos = new ObjectOutputStream(clientSoc.getOutputStream());
					ArrayList<UserDto> userlist = new UserBLL().getAllUser();
					oos.writeObject(userlist);
					oos.close();
					response = "226 User list send OK";
	            break;
	            case "SHARE":
	            	ObjectInputStream ois = new ObjectInputStream(clientSoc.getInputStream());
	            	try {
						ArrayList<Integer> usernameList = (ArrayList<Integer>)ois.readObject();
						new ShareBLL().setShare(usernameList, Integer.valueOf(params));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	ois.close();
	            	response = "226 User list send OK";
	            	break;
	            case "LSSHARE":
	            	oos = new ObjectOutputStream(clientSoc.getOutputStream());
					ArrayList<UserDto> userlist2 = new ShareBLL().getSharedList(Integer.valueOf(params));
					oos.writeObject(userlist2);
					oos.close();
					response = "226 User list send OK";
	            	break;
				case "LIST": {
					oos = new ObjectOutputStream(clientSoc.getOutputStream());
					//params = params.replaceAll("/", "\\\\");
					int parentID = new UploadBLL().parentID(baseDir + params);
					ArrayList<FileDto> result;
					if(producer.role.equals("user"))
						result = new FileBLL().getAllFiles(parentID, producer.UID);
					else
						result = new FileBLL().getAllFiles(parentID);
//					File f = new File (baseDir + params);
//					File[] files = f.listFiles();
//					ArrayList<FileDto> result = new ArrayList<FileDto>();
//					if(files != null) {
//						for(File file : files) {
//							FileDto fDto = new FileDto();
//							fDto.setName(file.getName());
//							Path p = Paths.get(file.getAbsolutePath());
//							BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
//							
//							fDto.setSize(attr.size());
//							fDto.setType(attr.isDirectory() ? "Dir" : "File");
//							DateFormat df=new SimpleDateFormat("DD/MM/YYYY");
//							fDto.setCreatedDate(new Date(attr.creationTime().toMillis()));
//							result.add(fDto);
//						}
//					}
//					else if(f.isFile()) {
//						FileDto fDto = new FileDto();
//						fDto.setName(f.getName());
//						Path p = Paths.get(f.getAbsolutePath());
//						BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
//						
//						fDto.setSize(attr.size());
//						fDto.setType(attr.isDirectory() ? "Dir" : "File");
//						DateFormat df=new SimpleDateFormat("DD/MM/YYYY");
//						fDto.setCreatedDate(new Date(attr.creationTime().toMillis()));
//						result.add(fDto);
//					}
					oos.writeObject(result);
					oos.close();
					response = "226 Directory send OK";
					break;
				}
				case "STOR":
					dis = new DataInputStream(clientSoc.getInputStream());
					dos = new DataOutputStream(new FileOutputStream(baseDir+params));
					
					buffer = new byte[MAX_BUFFER];
					read = 0;
					long size = dis.readLong();
					while((read = dis.read(buffer)) != -1) {
						dos.write(buffer, 0, read);
						dos.flush();
						buffer = new byte[MAX_BUFFER];
					}
						File file = new File (baseDir+params);
						if(file.length() == size) {
							if (producer.userName.equals("admin1")) {
						//File file = new File (baseDir+params);
						FileDto fDto = new FileDto();
						Path p = Paths.get(file.getAbsolutePath());
						BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
						fDto.setName(file.getName());
						fDto.setType(attr.isDirectory() ? "Dir" : "File");
						fDto.setPath(p.toString());
						//DateFormat df=new SimpleDateFormat("DD/MM/YYYY");
						fDto.setCreatedDate(new Date(attr.creationTime().toMillis()));
						if (new UploadBLL().checkFileExists(fDto)==false )
						{
							fDto.setOwner(producer.userName);
							//fDto.setCreatedDate(new Date(attr.lastModifiedTime().toMillis()));
							fDto.setLastEditedBy(producer.userName);
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
							if (new UploadBLL().uploadFile(fDto)) {
								response = "226 Transfer completed";
							}
							else {
								response = "502 Command not implemented";
							}
						}
						else {
							//fDto.setOwner("");
							dos.writeUTF("602 Directory Existed");
							
							//fDto.setCreatedDate(new Date(attr.lastModifiedTime().toMillis()));
							fDto.setLastEditedBy(producer.userName);
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
							if (new UploadBLL().uploadFile(fDto)) {
								response = "226 Transfer completed";
							}
							else {
								response = "502 Command not implemented";
							}
						}	
					}
					else {
						//File file = new File (baseDir+params);
						FileDto fDto = new FileDto();
						Path p = Paths.get(file.getAbsolutePath());
						BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
						fDto.setName(file.getName());
						fDto.setType(attr.isDirectory() ? "Dir" : "File");
						fDto.setPath(p.toString());
						//DateFormat df=new SimpleDateFormat("DD/MM/YYYY");
						fDto.setCreatedDate(new Date(attr.creationTime().toMillis()));
						FileDto base = new FileDto();
						FileDto base1 = new FileDto();
		                base.setPath(fDto.getPath());
		                base1.setPath(fDto.getPath().substring(0, fDto.getPath().lastIndexOf(File.separator)));
		                base.setFID(new UploadBLL().findFID(base));
		                base1.setFID(new UploadBLL().findFID(base1));
		                
						List<String> list = new UploadBLL().getAllOwner(base);
				        List<Integer> listFID = new UploadBLL().getAllFID(base);  
				        List<String> list1 = new UploadBLL().getAllOwner(base1);
				        List<Integer> listFID1 = new UploadBLL().getAllFID(base1);  
				        int dem=0;
				        for (int i=0;i<listFID.size();i++)
				        {
				        	List<String> listSh = new ArrayList<String>();
				        	listSh=new UploadBLL().getAllSharedByFID(listFID.get(i));
				        	for(int j=0;j<listSh.size();j++)
				        	{
				        		if (listSh.get(j).equals(producer.userName)) dem++;
				        	}
				        }
						List<String> listShared = new UploadBLL().getAllShared(base);
						
						for (int i=0;i<list.size();i++)
						{
							if(list.get(i).equals(producer.userName)) dem++;
						}
						for (int i=0;i<listShared.size();i++)
						{
							if(listShared.get(i).equals(producer.userName)) dem++;
						}
						
						for (int i=0;i<listFID1.size();i++)
				        {
				        	List<String> listSh = new ArrayList<String>();
				        	listSh=new UploadBLL().getAllSharedByFID(listFID1.get(i));
				        	for(int j=0;j<listSh.size();j++)
				        	{
				        		if (listSh.get(j).equals(producer.userName)) dem++;
				        	}
				        }
						if (dem>0) {
							
							if (new UploadBLL().checkFileExists(fDto)==false )
							{
								fDto.setOwner(producer.userName);
								//fDto.setCreatedDate(new Date(attr.lastModifiedTime().toMillis()));
								fDto.setLastEditedBy(producer.userName);
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
								if (new UploadBLL().uploadFile(fDto)) {
									response = "226 Transfer completed";
								}
								else {
									response = "502 Command not implemented";
								}
							}
							else {
								//fDto.setOwner("");
								dos.writeUTF("602 Directory Existed");
								System.out.println("602");
								//fDto.setCreatedDate(new Date(attr.lastModifiedTime().toMillis()));
								fDto.setLastEditedBy(producer.userName);
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
								if (new UploadBLL().uploadFile(fDto)) {
									response = "226 Transfer completed";
								}
								else {
									response = "502 Command not implemented";
								}
							}
						}
						else {
							dos.writeUTF("601 Permission denied");
							System.out.println("601");
						}
					}
						} else {
							file.delete();
							response = "503 Transfer stopped";
						}
					dis.close();
					dos.close();
					
					break;
				case "RETR":
					
					DataInputStream dis = new DataInputStream(new FileInputStream(baseDir+params));
					DataOutputStream dos = new DataOutputStream(clientSoc.getOutputStream());
					buffer = new byte[MAX_BUFFER];
					read = 0;
					while((read = dis.read(buffer)) != -1) {
						dos.write(buffer, 0, read);
						dos.flush();
						buffer = new byte[MAX_BUFFER];
					}
					response = "226 Transfer completed";
					dis.close();
					dos.close();
					break;
				default:
					
				}
	            clientSoc.close();
//	            soc.close();
	            
			
		} catch (IOException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();	
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public String getResponseMessage() {
		return response;
	}
}