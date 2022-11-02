package Client.view;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.text.*;
import java.util.ArrayList;

import javax.swing.*;

import Server.dto.FileDto;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class Client extends JFrame implements ActionListener, Runnable {
	private static final int MAX_BUFFER = 8192;
	private JPanel contentPane;
	private Socket soc;
	private DataOutputStream dos;
	private DataInputStream dis;
	private String server;
	
	private JTable table;
	DefaultTableModel dtm;
	private String workingDir = "/";

	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JProgressBar progressBar;
	private JButton btnBack;
	private JButton btnDownload;
	private JFileChooser chooser;
	private JButton btnUploadFile;
	private JButton btnDelete;
	private JButton btnNewFolder;
	private ArrayList<FileDto> files;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//Client frame = new Client();
					//frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private String showServerResponse() {
		String ch = "";
		try {
			ch = dis.readUTF();
			System.out.println(ch);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ch;
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
	
	private ArrayList<FileDto> listFiles (String path) {
		ArrayList<FileDto> files = null;
		try {
			dos.writeUTF("PASV");
			String response = showServerResponse();
			int port = Integer.valueOf(response.substring(response.indexOf("(")+1, response.indexOf(")")));
			Socket datasoc = new Socket(server, port);
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
	
	private void loadTable() {
		try {
			dtm.setRowCount(0);
			files = listFiles("");
			//showServerResponse();
			DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	        for (FileDto file : files) {
	        	String[] strarr = new String[4];
	            strarr[0] = file.getName();
//	            if(file.isDirectory()) {
//	            	int count = ftpClient.listFiles(workingDir + "/" + file.getName()).length;
//	            	strarr[1] =  count + " " + (count == 1 ? "item" : "items");
//	            }
	            strarr[1] = humanReadableByteCountBin(file.getSize());
	            strarr[2] = dateFormater.format(file.getCreatedDate().getTime());
	           // strarr[3] = file.getUser();
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
	
	private FileDto getFileInfo(String filename) {
		for(FileDto file : files) {
			if(file.getName().equals(filename)) return file;
		}
		return null;
	}
	
	private boolean makeDirectory(String dirName) {
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
		if(e.getSource() == btnNewFolder) {
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
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				File localFile = chooser.getSelectedFile();
				//localFile lúc này là cả đg dẫn đầy đủ, getName sẽ trả về chỉ tên file
				String filename = localFile.getName();
				//giống khi download, khi upload cug cần đg dẫn kèm cả tên file cần up lên
				String uploadDir = workingDir + filename;
				try {
					boolean done = true;
					if(localFile.isFile()) {
						done = uploadSingleFile(localFile, uploadDir);
					}
					else uploadDirectory(localFile, uploadDir);
					if (done) {
						loadTable();
						JOptionPane.showMessageDialog(null, "\"" + filename + "\"" + " is uploaded successfully.");
		            }
					else System.out.println("failed");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}			
			}
		}
		if(e.getSource() == btnDownload) {
			if(table.getSelectedRow() == -1) {
				JOptionPane.showMessageDialog(null, "No file selected.");
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
				      boolean success = true;
				      //nếu X là file thì gọi downloadSingleFile, nếu là folder thì gọi downloadDirectory
						      if(!getFileInfo(filename).getType().equals("Dir"))
						    		  success = downloadSingleFile(downloadPath, saveDir);
						      else {
						    	  File newDir = new File(saveDir);
						    	  boolean created = newDir.mkdirs();
					                if (created) {
					                    System.out.println("CREATED the directory: " + saveDir);
					                }
						    	  success = downloadDirectory(downloadPath, saveDir);
						      }
						      
				      if (success) {
				    	  JOptionPane.showMessageDialog(null, "\"" + filename + "\"" + " has been downloaded successfully.");
			            };
			      }
			} catch (Exception e1) {
				// TODO: handle exception
			}
		}
		if(e.getSource() == btnDelete) {
			if(table.getSelectedRow() == -1) {
				JOptionPane.showMessageDialog(null, "No file selected.");
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
					        JOptionPane.showMessageDialog(null, "\"" + filename + "\"" + " was deleted successfully.");
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
						        JOptionPane.showMessageDialog(null, "\"" + filename + "\"" + " was deleted successfully.");
						    } else {
						    	JOptionPane.showMessageDialog(null, "Could not delete the file.");
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
			showServerResponse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
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
	
	
	private boolean downloadSingleFile(String downloadPath, String saveDir) {
		try {
			dos.writeUTF("PASV");
			String response = showServerResponse();
			int port = Integer.valueOf(response.substring(response.indexOf("(")+1, response.indexOf(")")));
			Socket datasoc = new Socket(server, port);
			dos.writeUTF("RETR " + downloadPath);
			DataInputStream datadis = new DataInputStream(datasoc.getInputStream());
			DataOutputStream datados = new DataOutputStream(new FileOutputStream(saveDir));
			byte buffer[] = new byte[MAX_BUFFER];
			int read = 0;
			while((read = datadis.read(buffer)) != -1) {
				datados.write(buffer, 0, read);
				datados.flush();
				buffer = new byte[MAX_BUFFER];
			}
			datadis.close();
			datados.close();
			datasoc.close();
			showServerResponse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	//để tải 1 folder, thực hiện tải theo đệ quy
		//đầu tiên liệt kê hết folder và file có trong folder cần tải
		//nếu gặp file thì tải file đó về
		//nếu gặp folder thì tạo folder đó ở local, r gọi đệ quy hàm để tải folder con đó về
		public boolean downloadDirectory( String remoteDirPath, String saveDirPath) throws IOException {
		    String dirToList = remoteDirPath;
		    try {
		    	ArrayList<FileDto> subFiles = listFiles(dirToList);
				 
			    if (subFiles != null && subFiles.size() > 0) {
			        for (FileDto aFile : subFiles) {
			            String remoteFilePath = remoteDirPath + "/" + aFile.getName();
			            //lưu ý currentLocalPath có thể là dg dẫn tới folder hoặc file
			            String currentLocalPath = saveDirPath + File.separator + aFile.getName();
			            if (aFile.getType() == "Dir") {
			                // create the directory in saveDir
			                File newDir = new File(currentLocalPath);
			                boolean created = newDir.mkdirs();
			                if (created) {
			                    System.out.println("CREATED the directory: " + currentLocalPath);
			                } else {
			                	throw new Exception("COULD NOT create the directory: " + currentLocalPath);			                 
			                }
			                // download the sub directory
			                downloadDirectory(remoteFilePath, currentLocalPath);
			            } else {
			                // download the file
			                boolean success = downloadSingleFile(remoteFilePath, currentLocalPath);
			                if (success) {
			                    System.out.println("DOWNLOADED the file: " + currentLocalPath);
			                } else {
			                	throw new Exception("COULD NOT download the file: "+ currentLocalPath);
			                }
			            }
			        }
			    }
			} catch (Exception e) {
				// TODO: handle exception
				System.err.println(e);
				return false;
			}
		    return true;
		}
	
	
	private boolean uploadSingleFile(File localFile, String uploadDir) {
		try {
			dos.writeUTF("PASV");
			String response = showServerResponse();
			int port = Integer.valueOf(response.substring(response.indexOf("(")+1, response.indexOf(")")));
			Socket datasoc = new Socket(server, port);
			dos.writeUTF("STOR " + uploadDir);
			DataInputStream datadis = new DataInputStream(new FileInputStream(localFile));
			DataOutputStream datados = new DataOutputStream(datasoc.getOutputStream());
			byte buffer[] = new byte[MAX_BUFFER];
			int read = 0;
			while((read = datadis.read(buffer)) != -1) {
				datados.write(buffer, 0, read);
				datados.flush();
				buffer = new byte[MAX_BUFFER];
			}
			datadis.close();
			datados.close();
			datasoc.close();
			showServerResponse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} 
		
		return true;
	}
	
//	tương tự khi download, upload folder theo đệ quy
//		có thể coi File như 1 String chỉ đg dẫn bth
		public void uploadDirectory( File localDir, String remoteDirPath) throws IOException {	
			//tạo folder trên server
			boolean created = makeDirectory(remoteDirPath);
	        if (created) {
	            System.out.println("CREATED the directory: " + remoteDirPath);
	        } else {
	            System.out.println("COULD NOT create the directory: " + remoteDirPath);
	        }
	        //duyệt toàn bộ file trong folder cần upload
		    File[] subFiles = localDir.listFiles();
		    if (subFiles != null && subFiles.length > 0) {
		        for (File item : subFiles) {
		            String remoteFilePath = remoteDirPath + "/" + item.getName();
		            String currentLocalPath = localDir + File.separator + item.getName();
		            if (item.isFile()) {
		                // upload the file
		            	File localFile = new File(currentLocalPath);	               	            
		                boolean uploaded = uploadSingleFile(localFile, remoteFilePath);
		                if (uploaded) {
		                    System.out.println("UPLOADED a file to: " + remoteFilePath);
		                } else {
		                    System.out.println("COULD NOT upload the file: " + currentLocalPath);
		                }
		            } else {
		                // upload the sub directory
		                uploadDirectory( new File(currentLocalPath), remoteFilePath);
		            }
		        }
		    }
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
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	

	/**
	 * Create the frame.
	 */
	public Client(Socket soc, DataInputStream dis, DataOutputStream dos, String server) {
		this.soc = soc;
		this.dis = dis;
		this.dos = dos;
		this.server = server;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(400, 400, 700, 450);

		contentPane = new JPanel();
		//contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		contentPane.setLayout(null);
		setContentPane(contentPane);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		String[] columnNames = {"File name",
                "Size",
                "Date"};
		dtm = new DefaultTableModel(columnNames,0)  {
		    public boolean isCellEditable(int row, int column) {
		       return false;
		    }
		};
		table = new JTable(dtm);
		table.setShowGrid(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setBounds(12, 36, 500, 500);
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
		getContentPane().add(table);
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(26, 163, 643, 236);
		getContentPane().add(scrollPane);
		
		lblNewLabel = new JLabel("New label");
		lblNewLabel.setBounds(26, 99, 463, 15);
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
		progressBar.setBounds(74, 411, 502, 25);
		getContentPane().add(progressBar);
		progressBar.setVisible(false);
		lblNewLabel_1 = new JLabel("New label");
		lblNewLabel_1.setVisible(false);
		lblNewLabel_1.setBounds(581, 411, 70, 25);
		getContentPane().add(lblNewLabel_1);
		
		JLabel lblngDngTruyn = new JLabel("ỨNG DỤNG TRUYỀN TẢI FILE");
		lblngDngTruyn.setFont(new Font("Dialog", Font.BOLD, 18));
		lblngDngTruyn.setHorizontalAlignment(SwingConstants.CENTER);
		lblngDngTruyn.setBounds(12, 27, 657, 46);
		contentPane.add(lblngDngTruyn);
		btnBack.addActionListener(this);
		btnDownload.addActionListener(this);
		btnUploadFile.addActionListener(this);
		btnDelete.addActionListener(this);
		btnNewFolder.addActionListener(this);
		
		//sau khi load sẽ set đường dẫn hiện tại
		printCurrentDir();
		loadTable();
	}

	
}
