package Client.bll;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import Client.view.Client;
import Server.dto.FileDto;

public class TFTPTransfer extends SwingWorker<String, String> {
	public static final int TFTPPORT = 6900;
	public static final int BUFFSIZE = 32768 + 4;
	public static final short OP_RRQ = 1;
	public static final short OP_WRQ = 2;
	public static final short OP_DAT = 3;
	public static final short OP_ACK = 4;
	public static final short OP_ERR = 5;
	public static final short ERR_LOST = 0;
	public static final short ERR_FNF = 1;
	public static final short ERR_ACCESS = 2;
	public static final short ERR_EXISTS = 6;
	public static final String[] errorCodes = {"Not defined", "File not found.", "Access violation.", 
			"Disk full or allocation exceeded.", "Illegal TFTP operation.", 
			"Unknown transfer ID.", "File already exists.", 
			"No such user."};
	private String serverIP;
	public static String mode;
	private String requestType;
	private FileDto fileInfo;
	private String saveDir;
	private DatagramSocket socket;
	//private DatagramSocket serverSoc;
	private Client client;
	//private byte[] buff;
	
	public TFTPTransfer(String requestType, Client client, FileDto fileInfo, String serverIP, String saveDir) {
		this.requestType = requestType;
		this.fileInfo = fileInfo;
		this.serverIP = serverIP;
		this.saveDir = saveDir;
		this.client = client;
	}
	@Override
	protected void process(List<String> chunks) {
		client.lblTask.setText(chunks.get(chunks.size()-1));
	} 
	
	@Override
	protected String doInBackground() throws Exception {
		String filePath = fileInfo.getPath();
		socket = new DatagramSocket();
		InetAddress serverAddr = InetAddress.getByName(serverIP);
		
		//buff = new byte[BUFFSIZE];
		long start = System.nanoTime(); 
		if(requestType.equals("Download")) {
			downloadFile(filePath, serverAddr);
		} else {
			uploadFile(filePath, serverAddr);
		}
		long totalTime = System.nanoTime() - start;
		System.out.println(TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS));
		return null;
	}
	
	@Override
    protected void done() {
        if (!isCancelled()) {
        	String action = requestType.equals("Upload") ? "uploaded" : "downloaded";
            int ok = JOptionPane.showOptionDialog(null,
            		"\"" + fileInfo.getName() + "\"" + " has been " + action + " successfully!", "Message",
            		JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null, null, null);
            if(ok == JOptionPane.OK_OPTION || ok == JOptionPane.CANCEL_OPTION) {
            	client.progressBar.setVisible(false);
            	client.lblPercent.setText("");
            	client.lblTask.setText("");
            }
            client.loadTable();
        }
        client.isFileTransfering = false;
    }  
	
	//gửi request tới khi nhận dc ACK hoặc DATA (hoặc ERROR)
	private DatagramPacket sendRequest(short opRequest, String filePath, String mode, InetAddress serverAddr) {
		ByteBuffer wrap = ByteBuffer.allocate(BUFFSIZE);
		wrap.putShort(opRequest);
		wrap.put(filePath.getBytes());
		wrap.put((byte) 0);
//		wrap.put(mode.getBytes());
//		wrap.put((byte) 0);
		wrap.put(client.username.getBytes());
		wrap.put((byte) 0);
		
		DatagramPacket packet = new DatagramPacket(wrap.array(),wrap.array().length, serverAddr, TFTPPORT);
		int retryCount = 0;
		byte[] rec = new byte[BUFFSIZE];
		DatagramPacket receiver = new DatagramPacket(rec, rec.length);
		
		while(true) {
		    if (retryCount++ >= 6) {
		        System.err.println("Timed out. Closing connection.");
		        return null;
		    }
		    try {
		    	socket.send(packet);
		    	socket.setSoTimeout(3000);
		    	socket.receive(receiver);
		        
		        short blockNum = getData(receiver);	             
		        if ((blockNum == 1 && opRequest == OP_RRQ) || (blockNum == 0 && opRequest == OP_WRQ)) {
		        	return receiver;
		        } else if (blockNum == -1) {
		        	return null;
		        } else {
		        	//wrong DATA
		        	retryCount = 0;
		        	throw new SocketTimeoutException();
		        }
		    } catch (SocketTimeoutException e) {
		        System.out.println("Timeout.");
		    } catch (IOException e) {
				System.err.println("IO Error.");
			} finally {
		        try {
		        	socket.setSoTimeout(0);
				} catch (SocketException e) {
					System.err.println("Error resetting Timeout.");
				}
		    }
		}
		//return null;
	}
	
	private void uploadFile(String filePath, InetAddress serverAddr) {
		long fileSize = fileInfo.getSize();
		long uploaded = 0;
		int percentCompleted = 0;
		publish("Uploading " + fileInfo.getPath());
		byte[] buff = new byte[BUFFSIZE-4];
		
		DatagramPacket firstAck = sendRequest(OP_WRQ, filePath, "octet", serverAddr);
		
		if(firstAck == null) {
			System.err.println("Request timed out");
			return;
		}
		else {
			try {
				short blockNum = 1;
				socket.connect(firstAck.getAddress(), firstAck.getPort());
				FileInputStream fis = new FileInputStream(saveDir);
				while (true) {
					int length;
					try {
						length = fis.read(buff);
					} catch (IOException e) {
						System.err.println("Error reading file.");
						fis.close();
						return;
					}
					//khi packet data cuối có độ dài = BUFFSIZE, thì packet cuối có độ dài = 0, in.read() trả về -1
					if (length == -1) {
						length = 0;
					}
					DatagramPacket sender = dataPacket(blockNum, buff, length);
					if(WriteAndReadAck(socket, sender, blockNum++)) {
						uploaded += length;
						percentCompleted = (int) (uploaded * 100 / fileSize);
						setProgress(percentCompleted);
					} else {
						System.err.println("Error. Lost connection.");
						sendError(socket, ERR_LOST, "Lost connection.");
						return;
					}
					
					if (length < BUFFSIZE - 4) {
						try {
							fis.close();
						} catch (IOException e) {
							System.err.println("Trouble closing file.");
						}
						break;
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
	}
	
	private void downloadFile(String filePath, InetAddress serverAddr) {
		long fileSize = fileInfo.getSize();
		long downloaded = 0;
		int percentCompleted = 0;
		publish("Downloading " + fileInfo.getPath());
		
		DatagramPacket firstDataPack = sendRequest(OP_RRQ, filePath, "octet", serverAddr);
		
		if(firstDataPack == null) {
			System.err.println("Request timed out");
			return;
		}
		else {
			try {
				FileOutputStream fos = new FileOutputStream(saveDir);
				int state = writePacketToFile(fos, firstDataPack);
				downloaded += firstDataPack.getLength() - 4;
				percentCompleted = (int) (downloaded * 100 / fileSize);
				setProgress(percentCompleted);
				if(state == -1) {
					return;
				}
				else if (state == 0) {
					return;
				}
				
				//DatagramSocket dataSoc = new DatagramSocket();
				socket.connect(firstDataPack.getAddress(), firstDataPack.getPort());
				
				short blockNum = 1;
				while(true) {
					DatagramPacket dataPacket = ReadAndWriteData(socket, ackPacket(blockNum++), blockNum);
					if (dataPacket == null) {
						return;
					}
					else {
						state = writePacketToFile(fos, dataPacket);
						downloaded += firstDataPack.getLength() - 4;
						percentCompleted = (int) (downloaded * 100 / fileSize);
						setProgress(percentCompleted);
						if(state == -1) {
							sendError(socket,ERR_ACCESS, "Trouble writing data.");
							return;
						}
						else if (state == 0) {
							socket.send(ackPacket(blockNum));
							System.out.println("Done");
							return;
						}
					}
				}		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	//1: success
	//0: success & end of file
	//-1: error
	private int writePacketToFile(FileOutputStream fos, DatagramPacket dataPacket) {
		byte[] data = dataPacket.getData();
		try {
			fos.write(data, 4, dataPacket.getLength()-4);
			//System.out.println(dataPacket.getLength());
		} catch (IOException e) {
			System.err.println("IO Error writing data.");
			return -1;
		}
		if(dataPacket.getLength()-4 < BUFFSIZE - 4) {
			return 0;
		}
		return 1;
	}
	
	private DatagramPacket ReadAndWriteData(DatagramSocket serverSoc, DatagramPacket sendAck, short block) {
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
            	serverSoc.send(sendAck);
            	serverSoc.setSoTimeout(3000);
            	serverSoc.receive(receiver);
                
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
//                try {
//                	serverSoc.send(sendAck);
//				} catch (IOException e1) {
//					System.err.println("Error sending...");
//				}
            } catch (IOException e) {
				System.err.println("IO Error.");
			} finally {
                try {
                	serverSoc.setSoTimeout(0);
				} catch (SocketException e) {
					System.err.println("Error resetting Timeout.");
				}
            }
        }
	}
	
	private boolean WriteAndReadAck(DatagramSocket dataSoc, DatagramPacket sender, short blockNum) {
		int retryCount = 0;
		byte[] rec = new byte[BUFFSIZE];
		DatagramPacket receiver = new DatagramPacket(rec, rec.length);
		
		while(true) {
			if (retryCount >= 6) {
	            System.err.println("Timed out. Closing connection.");
	            return false;
	        }
	        try {
	        	dataSoc.send(sender);
	            System.out.println("Sent.");
	            dataSoc.setSoTimeout(3000);
	            dataSoc.receive(receiver);
	            
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
	            
	        } catch (SocketTimeoutException e) {
	            System.out.println("Timeout. Resending.");
	        } catch (IOException e) {
				System.err.println("IO Error. Resending.");
			} finally {
	            try {
	            	dataSoc.setSoTimeout(0);
				} catch (SocketException e) {
					System.err.println("Error resetting Timeout.");
				}
	        }
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
			parseError(buffer);
			return -1;
		}
		
		return buffer.getShort();
	} 
	
	private short getAck(DatagramPacket ack) {
		ByteBuffer buffer = ByteBuffer.wrap(ack.getData());
		short opcode = buffer.getShort();
		if (opcode == OP_ERR) {
			parseError(buffer);
			return -1;
		}
		//block num
		return buffer.getShort();
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
	
	private DatagramPacket dataPacket(short block, byte[] data, int length) {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFSIZE);
        buffer.putShort(OP_DAT);
        buffer.putShort(block);
        buffer.put(data, 0, length);
		
        return new DatagramPacket(buffer.array(), 4+length);
	} 

}
