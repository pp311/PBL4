package Client.view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JComboBox;
import java.awt.Choice;
import java.awt.Checkbox;
import javax.swing.ButtonGroup;
import javax.swing.JPasswordField;

public class Login extends JFrame implements ActionListener{
	private JPanel contentPane;
	private JTextField tf_sever;
	private JTextField tf_port;
	private JTextField tf_username;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton rb_Ftp;
	private JRadioButton rb_Tftp;
	private JPasswordField pf_password;
	private JButton btn_OK;
	private JButton btn_Cancel;
	static Login frame;
	private Socket soc;
	private DataInputStream dis;
	private DataOutputStream dos;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Login() {
		setTitle("Form Đăng Nhập");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 480, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Sever IP : ");
		lblNewLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
		lblNewLabel.setBounds(30, 186, 123, 46);
		contentPane.add(lblNewLabel);
		
		JLabel lblPort = new JLabel("Port : ");
		lblPort.setFont(new Font("Times New Roman", Font.BOLD, 18));
		lblPort.setBounds(30, 244, 123, 46);
		contentPane.add(lblPort);
		
		JLabel lblUsername = new JLabel("Username : ");
		lblUsername.setFont(new Font("Times New Roman", Font.BOLD, 18));
		lblUsername.setBounds(30, 70, 123, 46);
		contentPane.add(lblUsername);
		
		JLabel lblPassword = new JLabel("Password : ");
		lblPassword.setFont(new Font("Times New Roman", Font.BOLD, 18));
		lblPassword.setBounds(30, 128, 123, 46);
		contentPane.add(lblPassword);
		
		JLabel lblNewLabel_1 = new JLabel("LOGIN");
		lblNewLabel_1.setFont(new Font("Times New Roman", Font.BOLD, 30));
		lblNewLabel_1.setBounds(192, 0, 157, 71);
		contentPane.add(lblNewLabel_1);
		
		tf_sever = new JTextField();
		tf_sever.setBounds(156, 196, 265, 30);
		contentPane.add(tf_sever);
		tf_sever.setColumns(10);
		
		tf_port = new JTextField();
		tf_port.setColumns(10);
		tf_port.setBounds(156, 254, 265, 30);
		tf_port.setText("2100");
		contentPane.add(tf_port);
		
		
		tf_username = new JTextField();
		tf_username.setColumns(10);
		tf_username.setBounds(156, 80, 265, 30);
		contentPane.add(tf_username);
		
		btn_OK = new JButton("OK");
		btn_OK.setFont(new Font("Times New Roman", Font.BOLD, 16));
		btn_OK.setBounds(93, 347, 105, 39);
		contentPane.add(btn_OK);
		
		btn_Cancel = new JButton("Cancel");
		btn_Cancel.setFont(new Font("Times New Roman", Font.BOLD, 16));
		btn_Cancel.setBounds(264, 347, 105, 39);
		contentPane.add(btn_Cancel);
		
		rb_Tftp = new JRadioButton("TFTP");
		buttonGroup.add(rb_Tftp);
		rb_Tftp.setBounds(254, 298, 65, 27);
		contentPane.add(rb_Tftp);
		rb_Tftp.setFont(new Font("Times New Roman", Font.BOLD, 15));
		
		rb_Ftp = new JRadioButton("FTP");
		buttonGroup.add(rb_Ftp);
		rb_Ftp.setBounds(118, 298, 55, 27);
		contentPane.add(rb_Ftp);
		rb_Ftp.setSelected(true);
		rb_Ftp.setFont(new Font("Times New Roman", Font.BOLD, 15));
		
		pf_password = new JPasswordField();
		pf_password.setFont(new Font("Times New Roman", Font.BOLD, 16));
		pf_password.setBounds(156, 137, 265, 30);
		contentPane.add(pf_password);
		
		rb_Ftp.setVisible(false);
		rb_Tftp.setVisible(false);
		lblPort.setVisible(false);
		tf_port.setVisible(false);
		
		btn_OK.addActionListener(this);
		btn_Cancel.addActionListener(this);
		rb_Ftp.addActionListener(this);
		rb_Tftp.addActionListener(this);
	}
	String server;
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btn_OK)
    	{
			server = tf_sever.getText();
			String port = tf_port.getText();
			int portNum = Integer.valueOf(port);
			String user = tf_username.getText();
			String pw = new String(pf_password.getPassword());
			validate(server,port,user,pw);
			try {
				soc= new Socket(server, portNum);
				this.dis= new DataInputStream(soc.getInputStream());
				this.dos= new DataOutputStream(soc.getOutputStream());
				new ThreadedHandler(this).start();
				this.dos.writeUTF("LGIN " + user + " " + pw);
			} 
			catch(IOException e1){
				System.err.println(e1);
			}
			finally {
//				try {
//					dis.close();
//					dos.close();
//					soc.close();					
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
			}
    	}	
		//Khong quan trong
		if(e.getSource() == btn_Cancel)
    	{
    		System.exit(0);
    	}	
		if (rb_Ftp.isSelected()) 
		{
			tf_port.setText("2100");
		}
		if (rb_Tftp.isSelected()) 
		{
			tf_port.setText("69");
		}

	}
	
	private void validate(String server, String port, String user, String pw) {
		if (user.equals("") ||server.equals("")||port.equals("")||pw.equals("") ) 
			{JOptionPane.showMessageDialog(null, "Vui lòng nhập thông tin đăng nhập đầy đủ","Warning !!!",JOptionPane.WARNING_MESSAGE);}
//		else if (user.equals(us) && server.equals(sv) && port.equals(po) && pw.equals(pass)) 
//			{JOptionPane.showMessageDialog(this, "Đăng nhập thành công");}
//		else {JOptionPane.showMessageDialog(null, "Thông tin đăng nhập không hợp lệ","Warning !!!",JOptionPane.WARNING_MESSAGE);}
	}
	
	public class ThreadedHandler extends Thread{
		Login login;
		public ThreadedHandler(Login login){
			this.login=login;
			}
		public void run(){
			String ch="";
			try{
				while(true){
					ch= dis.readUTF();
					String code=ch.substring(0, ch.indexOf(" "));
					String msg=ch.substring(ch.indexOf(" ")+1);
					if(code.equals("230")) {
						System.out.println(ch);
						EventQueue.invokeLater(new Runnable() {			
							@Override
							public void run() {
								new Client(soc, dis, dos, server).setVisible(true);		
							}
						});
						
						frame.dispose();
						Thread.currentThread().interrupt();
						break;
					}
					else if(code.equals("530")) {
						JOptionPane.showMessageDialog(null, msg);
					}
				}
			}
			catch(IOException e){
				
				}
			}
		}
}