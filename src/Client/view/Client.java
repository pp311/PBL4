package Client.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingConstants;
import java.awt.Font;


public class Client extends JFrame implements ActionListener, Runnable {

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
	
	//hàm xử lí khi click vào table
	//khi click thì xác định dòng dc click => lấy dc tên file/folder => gọi changeWorkingDirectory
	//để ycau server đổi đường dẫn hiện tại => gọi loadData để reload lại bảng
	private void tableClick(JTable table) {
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();
		try {
//					ftpClient.changeWorkingDirectory(String.valueOf(table.getValueAt(row, 0)));
//					getCurrentWorkingDir();
//					loadData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnNewFolder) {
			try {
				dos.writeUTF("MKD " + workingDir);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	private void printCurrentDir() {
		try {
			dos.writeUTF("PWD");
			String dir = dis.readUTF();
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
                "Date",
                "User"};
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
//		btnBack.addActionListener(this);
//		btnDownload.addActionListener(this);
//		btnUploadFile.addActionListener(this);
//		btnDelete.addActionListener(this);
//		btnNewFolder.addActionListener(this);
//		btnLogin.addActionListener(this);
		
		
		
		printCurrentDir();
	}

	
}
