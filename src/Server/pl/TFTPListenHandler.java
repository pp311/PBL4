package Server.pl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Server.bll.UploadBLL;
import Server.bll.UserBLL;
import Common.dto.FileDto;
import Common.dto.UserDto;

public class TFTPListenHandler extends Thread{
	public static final int TFTPPORT = 6900;
	public static final int BUFFSIZE = 32768 + 4;
	public static final String BASE_DIR = "/home/shared";
	//public static final String BASE_DIR = "D:\\Tai xuong\\ftp";
	public static final short OP_RRQ = 1;
	public static final short OP_WRQ = 2;
	public static final short OP_DAT = 3;
	public static final short OP_ACK = 4;
	public static final short OP_ERR = 5;
	public static final short ERR_LOST = 0;
	public static final short ERR_FNF = 1;
	public static final short ERR_ACCESS = 2;
	public static final short ERR_EXISTS = 6;
	public static final short ERR_PERMISSION = 8;
	public static String mode;
	private String username;
	public static final String[] errorCodes = {"Not defined", "File not found.", "Access violation.", 
												"Disk full or allocation exceeded.", "Illegal TFTP operation.", 
												"Unknown transfer ID.", "File already exists.", 
												"No such user.", "Permission denied."};
	
	public TFTPListenHandler() {
	}
	
	public void run() {
		byte[] buff = new byte[BUFFSIZE];
		try {
			/* Create socket */
			DatagramSocket socket= new DatagramSocket(null);
			
			/* Create local bind point */
			SocketAddress localBindPoint= new InetSocketAddress(TFTPPORT);
			socket.bind(localBindPoint);
			
			while(true) {
				DatagramPacket receivePacket = new DatagramPacket(buff, buff.length);
				socket.receive(receivePacket);
				
				InetSocketAddress clientAddress = new InetSocketAddress(receivePacket.getAddress(),receivePacket.getPort());
				if (clientAddress == null) /* If clientAddress is null, an error occurred in receiveFrom()*/
					continue;
				
				StringBuffer requestedFile = new StringBuffer();
				int reqtype = ParseRQ(buff, requestedFile);
				
				FileWriter fw = new FileWriter("log.txt", true);
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
				fw.append(simpleDateFormat.format(new Date()) + " : " + username + " : " + (reqtype == OP_RRQ ? "RRQ " : "WRQ ") + requestedFile.toString() + " (TFTP)" + "\n");
				fw.close();
				new Thread() {
					public void run() {
						try {
							DatagramSocket clientSoc = new DatagramSocket(0);
							clientSoc.connect(clientAddress);
													
							System.out.printf("%s request for %s from %s using port %d\n",
									(reqtype == OP_RRQ)?"Read":"Write", requestedFile.toString(),
									clientAddress.getHostName(), clientAddress.getPort());  

							if (reqtype == OP_RRQ) {      /* read request */
								requestedFile.insert(0, BASE_DIR);
								HandleRQ(clientSoc, requestedFile.toString(), OP_RRQ, username);
							}
							else {                       /* write request */
								requestedFile.insert(0, BASE_DIR);
								HandleRQ(clientSoc, requestedFile.toString(),OP_WRQ, username);  
							}
							clientSoc.close();
						} catch (SocketException e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private short ParseRQ(byte[] buff, StringBuffer requestedFile) {
		ByteBuffer wrap = ByteBuffer.wrap(buff);
		short opcode = wrap.getShort();
		int delimiterIndex = -1;
		for (int i = 2; i < buff.length; i++) {
			if (buff[i] == 0) {
				delimiterIndex = i;
				break;
			}
		}
		if (delimiterIndex == -1) {
			System.err.println("Corrupt request packet. Shutting down.");
			return -1;
		}
		
		String fileName = new String(buff, 2, delimiterIndex-2);
		System.out.println("Requested file = " + fileName);
		requestedFile.append(fileName);
		
		for (int i = delimiterIndex+1; i < buff.length; i++) {
			if (buff[i] == 0) {
				String temp = new String(buff,delimiterIndex+1,i-(delimiterIndex+1));
				username = temp;
//				System.out.println("Transfer mode = " + temp);
//				mode = temp;
//				if (temp.equalsIgnoreCase("octet")) {
					return opcode;
//				} else {
//					System.err.println("TFTP: No mode specified.");
//				}
			}
		}
		System.err.println("TFTP: Did not find delimiter.");
		return 0;
	} 
	
	private void HandleRQ(DatagramSocket clientSoc, String string, int op, String username) {
		//System.out.println(string);
		File file = new File(string);
		byte[] buff = new byte[BUFFSIZE-4];
		
		if (op == OP_RRQ) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				System.err.println("File not found. Sending error packet.");
				sendError(clientSoc, ERR_FNF, "");
				return;
			}
			
			short blockNum = 1;
			
			while (true) {
				int length;
				try {
					length = in.read(buff);
				} catch (IOException e) {
					System.err.println("Error reading file.");
					return;
				}
				//khi packet data cuối có độ dài = BUFFSIZE, thì packet cuối có độ dài = 0, in.read() trả về -1
				if (length == -1) {
					length = 0;
				}
				DatagramPacket sender = dataPacket(blockNum, buff, length);
				System.out.println("Sending.........");
				if (WriteAndReadAck(clientSoc, sender, blockNum++)) {
					System.out.println("Success. Send another. blockNum = " + blockNum);
				} else {
					System.err.println("Error. Lost connection.");
					sendError(clientSoc, ERR_LOST, "Lost connection.");
					return;
				}
				
				if (length < BUFFSIZE - 4) {
					try {
						in.close();
					} catch (IOException e) {
						System.err.println("Trouble closing file.");
					}
					break;
				}
			}
			
		} else if (op == OP_WRQ) {
//			if (file.exists()) {
//				System.out.println("File already exists.");
//				sendError(clientSoc, ERR_EXISTS, "File already exists.");
//				return;
//			} else {
				FileOutputStream output = null;
				try {
					output = new FileOutputStream(file, false);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					sendError(clientSoc, ERR_ACCESS, "Could not create file.");
					return;
				}
				
				UserDto userInfo = new UserBLL().getCurrentUserInfo(username);
				FileDto fDto = new FileDto();
				String tam = string;
				fDto.setPath(tam.substring(0,tam.lastIndexOf("/")));
				int permission = new UploadBLL().getPermission(fDto);
				
				if (userInfo.getRole().equals("admin") || permission == 2 ) {
				
				short blockNum = 0;
				
				while (true) {
					DatagramPacket dataPacket = ReadAndWriteData(clientSoc, ackPacket(blockNum++), blockNum);
					
					if (dataPacket == null) {
						System.err.println("Error. Lost connection.");
						sendError(clientSoc,ERR_LOST, "Lost connection.");
						try {
							output.close();
						} catch (IOException e) {
							System.err.println("Could not close file.");
						}
						System.out.println("Deleting incomplete file.");
						file.delete();
						break;
						
					} else {
						byte[] data = dataPacket.getData();
						try {
							output.write(data, 4, dataPacket.getLength()-4);
							
							
							//System.out.println(dataPacket.getLength());
						} catch (IOException e) {
							System.err.println("IO Error writing data.");
							sendError(clientSoc,ERR_ACCESS, "Trouble writing data.");
						}
						if (dataPacket.getLength()-4 < BUFFSIZE - 4) {
							try {
								clientSoc.send(ackPacket(blockNum));
							} catch (IOException e1) {
								try {
									clientSoc.send(ackPacket(blockNum));
								} catch (IOException e) {
								}
							}
							System.out.println("All done writing file.");
							try {
								file = new File (string);
//									if (producer.role.equals("admin")) {
										//File file = new File (baseDir+params);
										fDto = new FileDto();
										Path p = Paths.get(file.getAbsolutePath());
										BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
										fDto.setName(file.getName());
										fDto.setType(attr.isDirectory() ? "Dir" : "File");
										fDto.setPath(p.toString());
										//DateFormat df=new SimpleDateFormat("DD/MM/YYYY");
										if (new UploadBLL().checkFileExists(fDto)==false )
										{
											fDto.setOwner(username);
											fDto.setLastEditedBy(username);
											fDto.setCreatedDate(new Date(attr.creationTime().toMillis()));
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
												System.out.println("Transfer completed");
											}
											else {
												System.out.println("Command not implemented");
											}
										}
										else {
											//fDto.setOwner("");
											//dos.writeUTF("602 Directory Existed");
											
											//fDto.setCreatedDate(new Date(attr.lastModifiedTime().toMillis()));
											
											fDto.setCreatedDate(new Date(attr.creationTime().toMillis()));
											fDto.setLastEditedBy(username);
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
												System.out.println("Transfer completed");
											}
											else {
												System.out.println("Command not implemented");
											}
										}
										fDto.setFID( new UploadBLL().findFID(fDto));
										List<Integer> listFID = new UploadBLL().getAllFID(fDto);
										for (int i=1;i<listFID.size();i++)
								        {
								        	if (new UploadBLL().uploadFolder(new java.sql.Timestamp(fDto.getLastEditedDate().getTime()), username,listFID.get(i)))
								        	{
								        		System.out.println("success");
								        	}
								        	else {
								        		System.out.println("fail");
								        	}	
									}
								output.close();
							} catch (IOException e) {
								System.err.println(e);
							}
							break;
						}
					}
				}
				} else {
					FileDto base = new FileDto();
					FileDto base1 = new FileDto();
//	                base.setPath(fDto.getPath());
					String temp = string;
					base.setPath(temp);
//	                base1.setPath(fDto.getPath().substring(0, fDto.getPath().lastIndexOf(File.separator)));
					base1.setPath(base.getPath().substring(0, base.getPath().lastIndexOf("/")));
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
			        		if (listSh.get(j).equals(username)) dem++;
			        	}
			        }
					List<String> listShared = new UploadBLL().getAllShared(base);
					
					for (int i=0;i<list.size();i++)
					{
						if(list.get(i).equals(username)) dem++;
					}
					for (int i=0;i<listShared.size();i++)
					{
						if(listShared.get(i).equals(username)) dem++;
					}
					
					for (int i=0;i<listFID1.size();i++)
			        {
			        	List<String> listSh = new ArrayList<String>();
			        	listSh=new UploadBLL().getAllSharedByFID(listFID1.get(i));
			        	for(int j=0;j<listSh.size();j++)
			        	{
			        		if (listSh.get(j).equals(username)) dem++;
			        	}
			        }
					if(dem <= 0) {
						System.err.println("Error. Permission denied.");
						sendError(clientSoc,ERR_PERMISSION, "Permission denied.");
						try {
							output.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return;
					}
					if (dem>0) {
						short blockNum = 0;
						
						while (true) {
							DatagramPacket dataPacket = ReadAndWriteData(clientSoc, ackPacket(blockNum++), blockNum);
							
							if (dataPacket == null) {
								System.err.println("Error. Lost connection.");
								sendError(clientSoc,ERR_LOST, "Lost connection.");
								try {
									output.close();
								} catch (IOException e) {
									System.err.println("Could not close file.");
								}
								System.out.println("Deleting incomplete file.");
								file.delete();
								break;
								
							} else {
								byte[] data = dataPacket.getData();
								try {
									output.write(data, 4, dataPacket.getLength()-4);
									
									
									//System.out.println(dataPacket.getLength());
								} catch (IOException e) {
									System.err.println("IO Error writing data.");
									sendError(clientSoc,ERR_ACCESS, "Trouble writing data.");
								}
								if (dataPacket.getLength()-4 < BUFFSIZE - 4) {
									try {
										clientSoc.send(ackPacket(blockNum));
									} catch (IOException e1) {
										try {
											clientSoc.send(ackPacket(blockNum));
										} catch (IOException e) {
										}
									}
									System.out.println("All done writing file.");
						try {
						file = new File (string);
						  fDto = new FileDto();
						  Path p = Paths.get(file.getAbsolutePath());
						  BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
						  fDto.setName(file.getName());
						  fDto.setType(attr.isDirectory() ? "Dir" : "File");
						  fDto.setPath(p.toString());
						
						if (new UploadBLL().checkFileExists(fDto)==false )
						{
							fDto.setOwner(username);
							fDto.setCreatedDate(new Date(attr.lastModifiedTime().toMillis()));
							fDto.setLastEditedBy(username);
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
								System.out.println("Transfer completed");
							}
							else {
								System.out.println("Command not implemented");
							}
						}
						else {
							//fDto.setOwner("");
							//dos.writeUTF("602 Directory Existed");
							//System.out.println("602");
							fDto.setCreatedDate(new Date(attr.lastModifiedTime().toMillis()));
							fDto.setLastEditedBy(username);
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
								System.out.println("Transfer completed");
							}
							else {
								System.out.println("Command not implemented");
							}
						}
						fDto.setFID( new UploadBLL().findFID(fDto));
						List<Integer> listFID2 = new UploadBLL().getAllFID(fDto);
						for (int i=1;i<listFID2.size();i++)
				        {
				        	if (new UploadBLL().uploadFolder(new java.sql.Timestamp(fDto.getLastEditedDate().getTime()), username,listFID2.get(i)))
				        	{
				        		System.out.println("success");
				        	}
				        	else {
				        		System.out.println("fail");
				        	}	
				        }
						output.close();
						} catch (Exception e) {
							System.err.println(e);
						}
					
				}
					else {
						
						System.out.println("601");
					}
				}
				
				break;
				
						} 
						} 
					} 
				}
			
		//}
		
	}
	
	//gửi ACK cho WRQ/DATA trước và nhận gói DATA tiếp theo
	private DatagramPacket ReadAndWriteData(DatagramSocket clientSoc, DatagramPacket sendAck, short block) {
		int retryCount = 0;
		byte[] rec = new byte[BUFFSIZE];
		DatagramPacket receiver = new DatagramPacket(rec, rec.length);

        while(true) {
            if (retryCount >= 6) {
                System.err.println("Timed out. Closing connection.");
                return null;
            }
            try {
            	System.out.println("sending ack for block: " + block);
            	clientSoc.send(sendAck);
            	clientSoc.setSoTimeout(1000);
            	clientSoc.receive(receiver);
                
                short blockNum = getData(receiver);
                System.out.println(blockNum + " " + block);
                if (blockNum == block) {
                	return receiver;
                } else if (blockNum == -1) {
                	return null;
                } else {
                	System.out.println("Duplicate.");
                	retryCount++;
                	throw new SocketTimeoutException();
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout.");
                retryCount++;
//                try {
//                	clientSoc.send(sendAck);
//				} catch (IOException e1) {
//					System.err.println("Error sending...");
//				}
            } catch (IOException e) {
				System.err.println("IO Error.");
				retryCount++;
			} finally {
                try {
                	clientSoc.setSoTimeout(0);
				} catch (SocketException e) {
					System.err.println("Error resetting Timeout.");
				}
            }
        }
	}
	
	private boolean WriteAndReadAck(DatagramSocket clientSoc, DatagramPacket sender, short blockNum) {
		int retryCount = 0;
		byte[] rec = new byte[BUFFSIZE];
		DatagramPacket receiver = new DatagramPacket(rec, rec.length);
		
		while(true) {
			if (retryCount >= 6) {
	            System.err.println("Timed out. Closing connection.");
	            return false;
	        }
	        try {
	        	clientSoc.send(sender);
	            System.out.println("Sent.");
	            clientSoc.setSoTimeout(1000);
	            clientSoc.receive(receiver);
	            
	            /* _______________ Dissect Datagram and Test _______________ */
	            short ack = getAck(receiver);
//	            System.out.println("Ack received: " + ack);
	            if (ack == blockNum) {
//	            	System.out.println("Received correct OP_ACK");
	            	return true;
	            } else if (ack == -1) {
	            	return false;
	            } else {
//	            	System.out.println("Ignore. Wrong ack.");
	            	retryCount++;
	            	throw new SocketTimeoutException();
	            }
	            /* ^^^^^^^^^^^^^^^ Dissect Datagram and Test ^^^^^^^^^^^^^^^ */
	            
	        } catch (SocketTimeoutException e) {
	            System.out.println("Timeout. Resending.");
	            retryCount++;
	        } catch (IOException e) {
				System.err.println("IO Error. Resending.");
				retryCount++;
			} finally {
	            try {
	            	clientSoc.setSoTimeout(0);
				} catch (SocketException e) {
					System.err.println("Error resetting Timeout.");
				}
	        }
		}
	} 
	
	
	private void sendError(DatagramSocket clientSoc, short errorCode, String errMsg) {
		
		ByteBuffer wrap = ByteBuffer.allocate(BUFFSIZE);
		wrap.putShort(OP_ERR);
		wrap.putShort(errorCode);
		wrap.put(errMsg.getBytes());
		wrap.put((byte) 0);
		
		DatagramPacket receivePacket = new DatagramPacket(wrap.array(),wrap.array().length);
		try {
			clientSoc.send(receivePacket);
		} catch (IOException e) {
			System.err.println("Problem sending error packet.");
			e.printStackTrace();
		}
		
	}
	
	private void parseError(ByteBuffer buffer) {
		
		short errCode = buffer.getShort();
		
		byte[] buf = buffer.array();
		for (int i = 4; i < buf.length; i++) {
			if (buf[i] == 0) {
				String msg = new String(buf, 4, i - 4);
				if (errCode > 7) errCode = 0;
				System.err.println(errorCodes[errCode] + ": " + msg);
				break;
			}
		}
		
	} 
	
	
	private DatagramPacket ackPacket(short block) {
		
		ByteBuffer buffer = ByteBuffer.allocate(BUFFSIZE);
        buffer.putShort(OP_ACK);
        buffer.putShort(block);
		
        return new DatagramPacket(buffer.array(), 4);
	} 
	
	private short getData(DatagramPacket data) {
		ByteBuffer buffer = ByteBuffer.wrap(data.getData());
		short opcode = buffer.getShort();
		if (opcode == OP_ERR) {
			System.err.println("Client is dead. Closing connection.");
			parseError(buffer);
			return -1;
		}
		
		return buffer.getShort();
	} 
	
	private short getAck(DatagramPacket ack) {
		ByteBuffer buffer = ByteBuffer.wrap(ack.getData());
		short opcode = buffer.getShort();
		if (opcode == OP_ERR) {
			System.err.println("Client is dead. Closing connection.");
			parseError(buffer);
			return -1;
		}
		//block num
		return buffer.getShort();
	} 
	
	private DatagramPacket dataPacket(short block, byte[] data, int length) {
		
		ByteBuffer buffer = ByteBuffer.allocate(BUFFSIZE);
        buffer.putShort(OP_DAT);
        buffer.putShort(block);
        buffer.put(data, 0, length);
		
        return new DatagramPacket(buffer.array(), 4+length);
	} 
	
}