package Client.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.CharacterIterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import Server.dto.FileDto;

import javax.swing.SwingConstants;
import java.awt.Font;


public class Client extends JFrame implements ActionListener, Runnable {
	private static final int MAX_BUFFER = 8192;
	private JPanel contentPane;
	private Socket soc;
	private DataOutputStream dos;
	private DataInputStream dis;
	
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
	
	private void loadTable() {
		try {
			dtm.setRowCount(0);
			dos.writeUTF("PASV");
			String response = dis.readUTF();
			int port = Integer.valueOf(response.substring(response.indexOf(" ")+1));
			Socket soc = new Socket("localhost", port);
			dos.writeUTF("LIST " + workingDir);
			ObjectInputStream oos = new ObjectInputStream(soc.getInputStream());
			files = (ArrayList<FileDto>)oos.readObject();
			
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
			soc.close();
		} catch (IOException | ClassNotFoundException e) {
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
						printCurrentDir();
						loadTable();
						break;
					}
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		chooser = new JFileChooser(); 
		if(e.getSource() == btnNewFolder) {
			try {
				dos.writeUTF("MKD " + workingDir);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if(e.getSource() == btnBack) {
			try {
				dos.writeUTF("CWD ..");
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
					//else uploadDirectory(localFile, uploadDir);
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
				String downloadPath = workingDir + "/" + filename;
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
		//				      if(!isDirectory(filename))
		//				    		  success = downloadSingleFile(downloadPath, saveDir);
		//				      else downloadDirectory(downloadPath, saveDir);
				      success = downloadSingleFile(downloadPath, saveDir);
				      //hàm downloadDirectory t chưa viết để return boolean
				      if (success) {
				    	  JOptionPane.showMessageDialog(null, "\"" + filename + "\"" + " has been downloaded successfully.");
			            }
			      }
			} catch (Exception e1) {
				// TODO: handle exception
			}
		}
	}
	
	private boolean downloadSingleFile(String downloadPath, String saveDir) {
		try {
			dos.writeUTF("PASV");
			String response = dis.readUTF();
			int port = Integer.valueOf(response.substring(response.indexOf(" ")+1));
			Socket soc = new Socket("localhost", port);
			dos.writeUTF("RETR " + downloadPath);
			DataInputStream dis = new DataInputStream(soc.getInputStream());
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(saveDir));
			byte buffer[] = new byte[MAX_BUFFER];
			int read = 0;
			while((read = dis.read(buffer)) != -1) {
				dos.write(buffer, 0, read);
				dos.flush();
				buffer = new byte[MAX_BUFFER];
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
		return true;
	}
	
	private boolean uploadSingleFile(File localFile, String uploadDir) {
		try {
			dos.writeUTF("PASV");
			String response = dis.readUTF();
			int port = Integer.valueOf(response.substring(response.indexOf(" ")+1));
			Socket soc = new Socket("localhost", port);
			dos.writeUTF("STOR " + uploadDir);
			DataInputStream dis = new DataInputStream(new FileInputStream(localFile));
			DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
			byte buffer[] = new byte[MAX_BUFFER];
			int read = 0;
			while((read = dis.read(buffer)) != -1) {
				dos.write(buffer, 0, read);
				dos.flush();
				buffer = new byte[MAX_BUFFER];
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			try {
				dis.close();
				dos.close();
				soc.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return true;
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
	public Client(Socket soc, DataInputStream dis, DataOutputStream dos) {
		this.soc = soc;
		this.dis = dis;
		this.dos = dos;
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
//		btnDelete.addActionListener(this);
//		btnNewFolder.addActionListener(this);
		
		//sau khi load sẽ set đường dẫn hiện tại
		printCurrentDir();
		loadTable();
	}

	
}
