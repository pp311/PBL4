package Client.view;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;

import Server.dto.FileDto;
import Server.dto.UserDto;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import Client.bll.DownloadTask;
import Client.bll.TFTPTransfer;
import Client.bll.UploadTask;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


public class Client extends JFrame implements ActionListener, Runnable, PropertyChangeListener, WindowListener {
	public static final int MAX_BUFFER = 8192;
	private JPanel contentPane;
	private Socket soc;
	public DataOutputStream dos;
	public DataInputStream dis;
	public String server;
	private static final int PORT_RANGE_START = 20000;
	private static final int PORT_RANGE_END = 22000;
	private JTable table;
	DefaultTableModel dtm;
	private String workingDir = "/";

	public volatile boolean isFileTransfering = false;
	private JLabel lblNewLabel;
	public JLabel lblPercent;
	public JLabel lblTask;
	public JProgressBar progressBar;
	private JButton btnBack;
	private JButton btnDownload;
	private JFileChooser chooser;
	private JButton btnUploadFile;
	private JButton btnDelete;
	private JButton btnNewFolder;
	private JButton btnShare;
	private JButton btnSettings;
	public JButton btnStop;
	private JButton btnAccountManage;
	private JButton btnChangePassword;
	private ArrayList<FileDto> files;
	private UserDto userInfo;
	public String defaultMode = "PASV";
	public String defaultMethod = "FTP";
	public String username = "";
	private UploadTask uploadTask;
	private DownloadTask downTask;
	private TFTPTransfer tftpTask;
	
	//ExecutorService threadPool = Executors.newFixedThreadPool(1);

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//Client frame = new Client(null, null, null, null, null);
					//frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public String showServerResponse() {
		String ch = "";
		try {
			ch = dis.readUTF();
			if (ch.substring(0, 1).equals("6")) {
				JOptionPane.showMessageDialog(null, ch);
			}
			else System.out.println(ch);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ch;
	}
	public Boolean showServerResponse1() {
		String ch = "";
		try {
			ch = dis.readUTF();
			if (ch.substring(0, 1).equals("6")) return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	public Socket connectToDataConnection() {
		int port = 0;
		Socket datasoc = null;
		ServerSocket dataServer = null;
		Random generator = new Random();
		try {
			if (this.defaultMode == "PASV") {
				dos.writeUTF("PASV");
				String response = showServerResponse();
				port = Integer.valueOf(response.substring(response.indexOf("(")+1, response.indexOf(")")));
				datasoc = new Socket(server, port);				
			}
			else {
				while(true) {
					 port = generator.nextInt((PORT_RANGE_END - PORT_RANGE_START) + 1) + PORT_RANGE_START;
					try {
						dataServer = new ServerSocket(port);
						break;
					} catch (IOException e) {
						//nếu đã có kết nối ở port dc chon thì sẽ có lôĩ
						//catch ở đây để vòng lặp while dc tiếp tuc lặp
					}
				}
				
				dos.writeUTF("PORT (" + getLocalIP() + "|" + port + ")");
				datasoc = dataServer.accept();
				showServerResponse();
			}
		}catch (Exception e) {
				// TODO: handle exception
			System.err.println(e);
			return null;
			}
		return datasoc;
	}
	
	private static String humanReadableByteCountBin(long bytes) {
	    long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
	    if (absB < 1024) {
	        return bytes + " B";
	    }
	    long value = absB;
	    CharacterIterator ci = new StringCharacterIterator("KMGTPE");
	    for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
	        value >>= 10;
	        ci.next();
	    }
	    value *= Long.signum(bytes);
	    return String.format("%.1f %ciB", value / 1024.0, ci.current());
	}
	
	public ArrayList<FileDto> listFiles (String path) {
		ArrayList<FileDto> files = null;
		Socket datasoc = null;
		try {
		datasoc = connectToDataConnection();
		dos.writeUTF("LIST " + (path != null && path != "" ? path : workingDir));
		ObjectInputStream oos = new ObjectInputStream(datasoc.getInputStream());
		files = (ArrayList<FileDto>)oos.readObject();
		oos.close();
		datasoc.close();
		showServerResponse();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return files;
	}
	
	public void loadTable() {
		try {
			dtm.setRowCount(0);
			files = listFiles("");
			//showServerResponse();
			DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	        for (FileDto file : files) {
	        	String[] strarr = new String[8];
	            strarr[0] = file.getName();
//	            if(file.isDirectory()) {
//	            	int count = ftpClient.listFiles(workingDir + "/" + file.getName()).length;
//	            	strarr[1] =  count + " " + (count == 1 ? "item" : "items");
//	            }
	            strarr[1] = file.getType().equals("Dir") ? "Directory" : "File";
	            strarr[2] = humanReadableByteCountBin(file.getSize());
	            strarr[3] = dateFormater.format(file.getLastEditedDate().getTime());
	            strarr[4] = file.getLastEditedBy();
	            strarr[5] = file.getOwner();
	            strarr[6] = file.getPermission() == 1 ? "readonly" : "write";
	            strarr[7] = file.isShared() ? "v" : "";
	            dtm.addRow(strarr);
	        }
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//hàm xử lí khi click vào table
	//khi click thì xác định dòng dc click => lấy dc tên file/folder => gọi changeWorkingDirectory
	//để ycau server đổi đường dẫn hiện tại => gọi loadData để reload lại bảng
	private void tableClick(JTable table) {
		if(isFileTransfering) {
			JOptionPane.showMessageDialog(null, "Vui lòng chờ quá trình truyển tải file hoàn tất để tiếp tục!");
			return;
		}
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();
		try {
				String fileName = String.valueOf(table.getValueAt(row, 0));
				
				for(FileDto file : files ) {
					if(file.getName().equals(fileName) && file.getType().equals("Dir")) {
						dos.writeUTF("CWD " + workingDir + fileName + "/");
						showServerResponse();
						printCurrentDir();
						loadTable();						
						break;
					}
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public FileDto getFileInfo(String filename) {
		for(FileDto file : files) {
			if(file.getName().equals(filename)) return file;
		}
		return null;
	}
	
	public boolean makeDirectory(String dirName) {
		try {
			dos.writeUTF("MKD " + dirName);
			showServerResponse();
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		chooser = new JFileChooser(); 
		
		if(e.getSource() == btnSettings) {
			Settings frame = new Settings(defaultMethod, defaultMode, this);
			frame.setVisible(true);
		}
		if(e.getSource() == btnAccountManage) {
			AccountManage frame = new AccountManage(this);
			frame.setVisible(true);
		}
		if(e.getSource() == btnChangePassword) {
			ChangePassword frame = new ChangePassword(this);
			frame.setVisible(true);
		}
		
		if(e.getSource() == btnShare) {
			if(isFileTransfering) {
				JOptionPane.showMessageDialog(null, "Vui lòng chờ quá trình truyển tải file hoàn tất để tiếp tục!");
				return;
			}
			if(table.getSelectedRow() == -1) {
				JOptionPane.showMessageDialog(null, "No file selected.");
				return;
			}
			String filename = String.valueOf(table.getValueAt(table.getSelectedRow(), 0));
			FileDto fileDto = getFileInfo(filename);
			if(!fileDto.getOwner().equals(username)) {
				JOptionPane.showMessageDialog(null, "Permission denied.");
				return;
			}
			Share frame = new Share(fileDto, this);
			frame.setVisible(true);
		}
		
		if(e.getSource() == btnNewFolder) {
			if(isFileTransfering) {
				JOptionPane.showMessageDialog(null, "Vui lòng chờ quá trình truyển tải file hoàn tất để tiếp tục!");
				return;
			}
			String name = JOptionPane.showInputDialog(null,"Enter Name");
			if (name !=null) {
			try {
				makeDirectory(workingDir + name);
				printCurrentDir();
				loadTable();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			}
		}
		if(e.getSource() == btnBack) {
			if(isFileTransfering) {
				JOptionPane.showMessageDialog(null, "Vui lòng chờ quá trình truyển tải file hoàn tất để tiếp tục!");
				return;
			}
			if(workingDir.equals("/")) return;
			try {
				dos.writeUTF("CWD ..");
				showServerResponse();
				printCurrentDir();
				loadTable();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if(e.getSource() == btnUploadFile) {
			if(isFileTransfering) {
				JOptionPane.showMessageDialog(null, "Vui lòng chờ quá trình truyển tải file hoàn tất để tiếp tục!");
				return;
			}
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				File localFile = chooser.getSelectedFile();
				//localFile lúc này là cả đg dẫn đầy đủ, getName sẽ trả về chỉ tên file
				String filename = localFile.getName();
				//giống khi download, khi upload cug cần đg dẫn kèm cả tên file cần up lên
				String uploadDir = workingDir + filename;
				try {
					progressBar.setValue(0);
				    progressBar.setVisible(true);
				    btnStop.setVisible(true);
				    lblPercent.setVisible(true);
				    if(!defaultMethod.equals("TFTP")) {
				    	uploadTask = new UploadTask(localFile, uploadDir, this, defaultMode);
				    	uploadTask.addPropertyChangeListener(this);
				    	uploadTask.execute();
						isFileTransfering = true;
				    }
				    else {
				    	
				    	if(localFile.isDirectory()) {
				    		JOptionPane.showMessageDialog(null, "Không hỗ trợ upload thư mục đối với TFTP");
				    		return;
				    	}
				    	FileDto fileInfo = new FileDto();
				    	Path p = Paths.get(localFile.getAbsolutePath());
						BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
				    	fileInfo.setName(filename);
				    	fileInfo.setPath(workingDir + filename);
				    	fileInfo.setSize(attr.size());
				    	if(attr.size() > 32500*32500) {
				    		JOptionPane.showMessageDialog(null, "TFTP chỉ hỗ trợ upload file dưới 1GB");
				    		return;
				    	}
				    	
				    	tftpTask = new TFTPTransfer("Upload", this, fileInfo, server, localFile.getAbsolutePath());
				    	tftpTask.addPropertyChangeListener(this);
				    	tftpTask.execute();
						isFileTransfering = true;
				    }
					
//					boolean done = true;
//					if(localFile.isFile()) {
//						//done = uploadSingleFile(localFile, uploadDir);
//						UploadTask task = new UploadTask(localFile, uploadDir, this);
//						task.execute();
//					}
//					else uploadDirectory(localFile, uploadDir);
//					if (done) {
//						loadTable();
//						JOptionPane.showMessageDialog(null, "\"" + filename + "\"" + " is uploaded successfully.");
//		            }
//					else System.out.println("failed");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}			
			}
		}
		if(e.getSource() == btnDownload) {
			if(isFileTransfering) {
				JOptionPane.showMessageDialog(null, "Vui lòng chờ quá trình truyển tải file hoàn tất để tiếp tục!");
				return;
			}
			if(table.getSelectedRow() == -1) {
				JOptionPane.showMessageDialog(null, "No file selected.");
				return;
			}
			try {
				String filename = String.valueOf(table.getValueAt(table.getSelectedRow(), 0));
				String downloadPath = workingDir + filename;
				//chọn vị trí Download về nên chỉ cho phép chọn folder/directory
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				//gọi showSaveDialog để hiện cửa sổ chọn folder, khi nhấn Save thì mới thực hiện tiếp
				if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
						//getSelectedFile trả về đối tượng File, "" + để ép kiểu về String
				      String saveDir = "" + chooser.getSelectedFile();
				      //saveDir là đường dẫn tới folder lưu, để tải file cần đường dẫn với tên file cụ thể
				      //VD: tải file bt.pdf thì đường dẫn lưu cần chỉ chính xác home/Downloads/bt.pdf
				      
				      //dùng File.separator để xác định dấu phân cách, windows dùng \ nhưng Linux dùng /
				      //khi làm việc với đường dẫn của server thì có thể ghi thẳng / vì server là Linux
				      //nhưng khi làm việc với dg dẫn của client thì dùng File.separator
				      saveDir += File.separator + filename;
				      //boolean success = true;
				      progressBar.setValue(0);
				      progressBar.setVisible(true);
				      btnStop.setVisible(true);
				      lblPercent.setVisible(true);
				      if(!defaultMethod.equals("TFTP")) {
					      //nếu X là file thì gọi downloadSingleFile, nếu là folder thì gọi downloadDirectory
						      if(!getFileInfo(filename).getType().equals("Dir")) {
						    		 // success = downloadSingleFile(downloadPath, saveDir);
						    	  downTask = new DownloadTask(downloadPath, saveDir, this, false, defaultMode);
								}
						      else {
						    	  File newDir = new File(saveDir);
						    	  boolean created = newDir.mkdirs();
					                if (created) {
					                    System.out.println("CREATED the directory: " + saveDir);
					                }
					                downTask = new DownloadTask(downloadPath, saveDir, this, true, defaultMode);
						    	  //success = downloadDirectory(downloadPath, saveDir);
						      }
						      downTask.addPropertyChangeListener(this);
						      downTask.execute();
						      isFileTransfering = true;
				      }
				      else {
				    	  FileDto fileInfo = getFileInfo(filename);
				    	  if(fileInfo.getType().equals("Dir")) {
					    		JOptionPane.showMessageDialog(null, "Không hỗ trợ download thư mục đối với TFTP");
					    		return;
					    	}
				    	  if(fileInfo.getSize() > 32500*32500) {
					    		JOptionPane.showMessageDialog(null, "TFTP chỉ hỗ trợ download file dưới 1GB");
					    		return;
					    	}
				    	  fileInfo.setPath(workingDir + filename);
				    	  tftpTask = new TFTPTransfer("Download", this, fileInfo, server, saveDir);
				    	  tftpTask.addPropertyChangeListener(this);
				    	  tftpTask.execute();
					      isFileTransfering = true;
				      }
				      
//				      if (success) {
//				    	  JOptionPane.showMessageDialog(null, "\"" + filename + "\"" + " has been downloaded successfully.");
//			            };
			      }
			} catch (Exception e1) {
				// TODO: handle exception
			}
		}
		if (e.getSource() == btnStop) {
			if(downTask != null) {
				downTask.cancel(true);
			} 
			if (uploadTask != null) {
				uploadTask.cancel(true);
			} 
			if (tftpTask != null) {
				tftpTask.cancel(true);
			}
			isFileTransfering = false;
		}
		if(e.getSource() == btnDelete) {
			if(isFileTransfering) {
				JOptionPane.showMessageDialog(null, "Vui lòng chờ quá trình truyển tải file hoàn tất để tiếp tục!");
				return;
			}
			if(table.getSelectedRow() == -1) {
				JOptionPane.showMessageDialog(null, "No file selected.");
				return;
			}
			String filename = String.valueOf(table.getValueAt(table.getSelectedRow(), 0));
			int response = JOptionPane.showConfirmDialog(null, "Do you really want to delete " +filename + "?", "Confirm",
			        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.YES_OPTION) {
				String deletePath = workingDir + filename;
				//nếu chọn file thì gọi deleteFile, chọn folder thì gọi removeDirectory
				if(!getFileInfo(filename).getType().equals("Dir")) {
					try {
					    boolean deleted = deleteFile(deletePath);
					    if (deleted) {
							loadTable();
							//if (showServerResponse1()==true) {
					        JOptionPane.showMessageDialog(null, "\"" + filename + "\"" + " was deleted successfully.");
							//}
					    } else {
					    	JOptionPane.showMessageDialog(null, "Could not delete the file.");
					    }
					} catch (Exception ex) {
					    System.out.println("Oh no, there was an error: " + ex.getMessage());
					}
				} else {				 
					try {
					    	boolean deleted = deleteFile(deletePath);
						    if (deleted) {
								loadTable();
								//if (showServerResponse1()==true) {
						        JOptionPane.showMessageDialog(null, "\"" + filename + "\"" + " was deleted successfully.");
								//}
						    } else {
						    	JOptionPane.showMessageDialog(null, "Permission denied.");
						    }			        			    			      				    
					} catch (Exception ex) {
					    System.out.println("Oh no, there was an error: " + ex.getMessage());
					}
				}
			    } else if (response == JOptionPane.NO_OPTION || response == JOptionPane.CLOSED_OPTION) {
			      
			    } 
		}
	}
	
	private boolean deleteFile(String deletePath) {
		try {
			dos.writeUTF("DELE " + deletePath);	
			return showServerResponse1();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
//	public boolean deleteDirectory(String deletePath) throws IOException {
//        String dirToList = deletePath;
//        ArrayList<FileDto> subFiles = listFiles(dirToList);
//        if (subFiles != null && subFiles.size() > 0) {
//            for (FileDto aFile : subFiles) {
//                String currentFileName = aFile.getName();
//                
//                //filePath có thể là file hoặc thư mục
//                String filePath = dirToList + "/" + currentFileName;
// 
//                if (aFile.getType().equals("Dir")) {
//                    //xóa đệ quy thư mục con
//                    deleteDirectory(filePath);
//                } else {
//                    //xóa file
//                    boolean deleted = deleteFile(filePath);
//                    if (deleted) {
//                        System.out.println("DELETED the file: " + filePath);
//                    } else {
//                        System.out.println("CANNOT delete the file: " + filePath);
//                    }
//                }
//            }
//        }
//            // finally, remove the directory itself
//            boolean removed = deleteDirectory(dirToList);
//            if (removed) {
//                System.out.println("REMOVED the directory: " + dirToList);
//            } else {
//                System.out.println("CANNOT remove the directory: " + dirToList);
//            }
//            return removed;
//        
//    }
	public UserDto getUserInfo(String username) {
		UserDto userDto = null;
		Socket datasoc = null;
		try {
			datasoc = connectToDataConnection();
			dos.writeUTF("UINFO " + username);
			ObjectInputStream ois = new ObjectInputStream(datasoc.getInputStream());
			userDto = (UserDto)ois.readObject();
			ois.close();
			datasoc.close();
			showServerResponse();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userDto;
	}
	
	
	private void printCurrentDir() {
		try {
			dos.writeUTF("PWD");
			String dir = dis.readUTF();
			//server phản hồi dạng PWD + dg dẫn
			dir = dir.substring(dir.indexOf(" ") + 1);
			workingDir = dir;
			lblNewLabel.setText(dir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public ArrayList<UserDto> getAllUser () {
		ArrayList<UserDto> userList = null;
		Socket datasoc = null;
		try {
			datasoc = connectToDataConnection();
			dos.writeUTF("LSUSER all");
			ObjectInputStream ois = new ObjectInputStream(datasoc.getInputStream());
			userList = (ArrayList<UserDto>)ois.readObject();
			ois.close();
			datasoc.close();
			showServerResponse();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userList;
	}
	public boolean createAccount(UserDto u) {
		Socket datasoc = null;
		try {
			datasoc = connectToDataConnection();
			dos.writeUTF("ADDUSER " + 1);
			ObjectOutputStream oos = new ObjectOutputStream(datasoc.getOutputStream());
			oos.writeObject(u);
			oos.close();
			datasoc.close();
			return showServerResponse1();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public void setShare(ArrayList<Integer> uidList, int fid) {
		Socket datasoc = null;
		try {
			datasoc = connectToDataConnection();
			dos.writeUTF("SHARE " + fid);
			ObjectOutputStream oos = new ObjectOutputStream(datasoc.getOutputStream());
			oos.writeObject(uidList);
			oos.close();
			datasoc.close();
			showServerResponse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	public void setAnyonePermission(int FID, int perm) {
		String command = perm == 2 ? "SETWR" : "SETRD";
		try {
			dos.writeUTF(command + " " + FID);
			showServerResponse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<UserDto> getSharedUser(int FID) {
		ArrayList<UserDto> userList = null;
		Socket datasoc = null;
		try {
			datasoc = connectToDataConnection();
			dos.writeUTF("LSSHARE " + FID);
			ObjectInputStream ois = new ObjectInputStream(datasoc.getInputStream());
			userList = (ArrayList<UserDto>)ois.readObject();
			ois.close();
			datasoc.close();
			showServerResponse();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userList;
	}
	public boolean checkPassword(String pass) {
		try {
			dos.writeUTF("LGIN " + username + " " + pass);
			String s = dis.readUTF();
			String code=s.substring(0, s.indexOf(" "));
			String msg=s.substring(s.indexOf(" ")+1);
			if(code.equals("230")) { 
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return false;
	}
	public boolean changePassword(String pass) {
		try {
			dos.writeUTF("CHGPASS " + pass);
			return showServerResponse1();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public String getLocalIP() {
		String ipAddr = "";
		Enumeration<NetworkInterface> nets;
		try {
			nets = NetworkInterface.getNetworkInterfaces();
		
		for (NetworkInterface netint : Collections.list(nets)) {
		    if (!netint.isLoopback()) {
		        //theOneAddress = Collections.list(netint.getInetAddresses()).stream().findFirst().orElse(null);
		    	ArrayList<InetAddress> list = Collections.list(netint.getInetAddresses());
		    	for(int i = 0; i < list.size(); i++) {
		    		if(list.get(i).toString().contains(":")) {
		    			continue;
		    		}
		    		else {
		    			ipAddr = list.get(i).toString();
		    			ipAddr = ipAddr.substring(1);
		    		}
		    	}
		    }
		}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ipAddr;
	}
	

	/**
	 * Create the frame.
	 */
	public Client(Socket soc, DataInputStream dis, DataOutputStream dos, String server, String username) {
		this.soc = soc;
		this.dis = dis;
		this.dos = dos;
		this.server = server;
		this.username = username;
		
		Socket datasoc = null;
		try {
			datasoc = connectToDataConnection();
			dos.writeUTF("UINFO " + username);
			ObjectInputStream ois = new ObjectInputStream(datasoc.getInputStream());
			userInfo = (UserDto)ois.readObject();
			ois.close();
			datasoc.close();
			showServerResponse();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(400, 400, 1000, 650);

		contentPane = new JPanel();
		//contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		contentPane.setLayout(null);
		setContentPane(contentPane);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		String[] columnNames = null;
		if (userInfo.getRole().equals("admin")) {
			columnNames = new String[] {"Name",
					"Type",
	                "Size",
	                "Last Edited Date",
	                "Last Edited By",
	                "Owner",
	                "Permission"
			};
		} else {
			columnNames = new String[] {"Name",
					"Type",
	                "Size",
	                "Last Edited Date",
	                "Last Edited By",
	                "Owner",
	                "Permission",
	                "Shared"
			};
		}
		dtm = new DefaultTableModel(columnNames,0)  {
		    public boolean isCellEditable(int row, int column) {
		       return false;
		    }
		};
		table = new JTable(dtm);
		table.setShowGrid(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setBounds(12, 36, 800, 650);
		table.addMouseListener(new MouseAdapter() {
	         public void mouseClicked(MouseEvent me) {
	            if (me.getClickCount() == 2) {     // to detect doble click events
	               JTable target = (JTable)me.getSource();
	               tableClick(target);
	            }
	         }
	      });
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		table.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
		table.getColumnModel().getColumn(2).setCellRenderer( centerRenderer );
		table.getColumnModel().getColumn(3).setCellRenderer( centerRenderer );
		table.getColumnModel().getColumn(4).setCellRenderer( centerRenderer );
		table.getColumnModel().getColumn(5).setCellRenderer( centerRenderer );
		table.getColumnModel().getColumn(6).setCellRenderer( centerRenderer );
		if(!userInfo.getRole().equals("admin"))
			table.getColumnModel().getColumn(7).setCellRenderer( centerRenderer );
		getContentPane().add(table);
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(26, 163, 937, 314);
		getContentPane().add(scrollPane);
		
		lblNewLabel = new JLabel("New label");
		lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 14));
		lblNewLabel.setBounds(26, 85, 640, 29);
		getContentPane().add(lblNewLabel);
		
		btnBack = new JButton("Back");
		btnBack.setBounds(26, 126, 67, 25);
		getContentPane().add(btnBack);
		
		btnDownload = new JButton("Download");
		btnDownload.setBounds(363, 126, 105, 25);
		getContentPane().add(btnDownload);
		
		btnUploadFile = new JButton("Upload");
		btnUploadFile.setBounds(105, 126, 113, 25);
		getContentPane().add(btnUploadFile);
		
		btnDelete = new JButton("Delete");
		btnDelete.setBounds(480, 126, 81, 25);
		getContentPane().add(btnDelete);
		
		btnNewFolder = new JButton("New Folder");
		btnNewFolder.setBounds(230, 126, 121, 25);
		getContentPane().add(btnNewFolder);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(36, 544, 824, 25);
		getContentPane().add(progressBar);
		progressBar.setVisible(false);
		lblPercent = new JLabel("");
		lblPercent.setBounds(864, 544, 70, 25);
		getContentPane().add(lblPercent);
		
		JLabel lblngDngTruyn = new JLabel("ỨNG DỤNG TRUYỀN TẢI FILE");
		lblngDngTruyn.setFont(new Font("Dialog", Font.BOLD, 18));
		lblngDngTruyn.setHorizontalAlignment(SwingConstants.CENTER);
		lblngDngTruyn.setBounds(12, 27, 951, 46);
		contentPane.add(lblngDngTruyn);
		
		btnShare = new JButton("Share");
		btnShare.setBounds(573, 126, 93, 25);
		contentPane.add(btnShare);
		
		lblTask = new JLabel("");
		lblTask.setFont(new Font("Dialog", Font.BOLD, 14));
		lblTask.setBounds(36, 503, 825, 29);
		contentPane.add(lblTask);
		
		btnSettings = new JButton("Settings");
		btnSettings.setBounds(680, 126, 105, 25);
		contentPane.add(btnSettings);
		
		btnStop = new JButton("Stop");
		btnStop.setBounds(420, 581, 117, 25);
		btnStop.setVisible(false);
		contentPane.add(btnStop);
		
		btnAccountManage = new JButton("Account Manage");
		btnAccountManage.setBounds(800, 126, 163, 25);
		contentPane.add(btnAccountManage);
		
		btnChangePassword = new JButton("Change Password");
		btnChangePassword.setBounds(800, 126, 163, 25);
		contentPane.add(btnChangePassword);
		
		if(!userInfo.getRole().equals("admin")) {
			btnAccountManage.setVisible(false);
		} else {
			btnChangePassword.setVisible(false);
		}
		
		btnStop.addActionListener(this);
		btnShare.addActionListener(this);
		btnBack.addActionListener(this);
		btnDownload.addActionListener(this);
		btnUploadFile.addActionListener(this);
		btnDelete.addActionListener(this);
		btnNewFolder.addActionListener(this);
		btnSettings.addActionListener(this);
		btnAccountManage.addActionListener(this);
		btnChangePassword.addActionListener(this);
		this.addWindowListener(this);
		//progressBar.addPropertyChangeListener(this);
		//sau khi load sẽ set đường dẫn hiện tại
		
		
		
		printCurrentDir();
		loadTable();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		if(evt.getPropertyName() == "progress") {
			int progress = (int) evt.getNewValue();
            progressBar.setValue(progress);
            lblPercent.setText("" + progress + "%");
		}
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		int i=JOptionPane.showOptionDialog(null, "Bạn có thật sự muốn thoát?", "Message",JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, null, null);
        if(i==JOptionPane.YES_OPTION) {
        	try {
				dos.writeUTF("QUIT");
				Thread.sleep(100);
				//showServerResponse();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	System.exit(0);
        } else {
        	return;
        }
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
}
