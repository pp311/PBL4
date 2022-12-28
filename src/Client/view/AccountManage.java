package Client.view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import Common.dto.UserDto;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.SwingConstants;
import javax.swing.JSeparator;
import javax.swing.Box;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class AccountManage extends JFrame implements ActionListener {

	private JPanel contentPane;
	private JTextField tf_username;
	private JTextField tf_fullname;
	private JTextField tf_phone;
	private JTextField tf_email;
	private ArrayList<UserDto> userList;
	private ArrayList<String> usernameList;
	private CustomListModel<String> dlmUsers;
 	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AccountManage frame = new AccountManage(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnExit) {
			this.dispose();
		}
		if(e.getSource() == btnCreate) {
			String username = tf_username.getText();
			String fullname = tf_fullname.getText();
			String password = String.valueOf(pf_password.getPassword());
			String phone = tf_phone.getText();
			String email = tf_email.getText();
			if(username.equals("") || username == null ||
					fullname.equals("") || fullname == null ||
					password.equals("") || password == null ||
					phone.equals("") || phone == null ||
					email.equals("") || email == null ) {
				JOptionPane.showMessageDialog(null, "Vui lòng nhập đầy đủ các trường");
			} else {
				boolean isExistedUsername = false;
				for (UserDto userDto : userList) {
					if(userDto.getUserName().equals(username)) {
						isExistedUsername = true;
						break;
					}
				}
				if(isExistedUsername) {
					JOptionPane.showMessageDialog(null, "Username này đã tồi tại!");
				} else {
					UserDto u = new UserDto();
					u.setEmail(email);
					u.setFullName(fullname);
					u.setPassword(password);
					u.setPhone(phone);
					u.setRole("user");
					u.setUserName(username);
					boolean res = client.createAccount(u);
					if(res) {
						JOptionPane.showMessageDialog(null, "Tạo tài khoản thành công!");
						this.userList = client.getAllUser();
						dlmUsers = new CustomListModel<String>(new ArrayList<String>());
						this.userList.stream().forEach(user -> {
							dlmUsers.addElement(user.getUserName());
						});
						list.setModel(dlmUsers);
					} else {
						JOptionPane.showMessageDialog(null, "Có lỗi xảy ra.Tạo tài khoản thất bại");
					}
				}
			}
		}
		
	}

	/**
	 * Create the frame.
	 */
	JButton btnExit;
	JButton btnCreate;
	JList<String> list;

	
	private Client client;
	private JPasswordField pf_password;
	public AccountManage(Client client) {
		this.client = client;
		this.userList = client.getAllUser();

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 800, 700);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Account Manage");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 18));
		lblNewLabel.setBounds(12, 31, 773, 28);
		contentPane.add(lblNewLabel);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(0, 307, 797, 11);
		contentPane.add(separator);
		
		JLabel lblCreateNewAccount = new JLabel("Create new account: ");
		lblCreateNewAccount.setFont(new Font("Dialog", Font.BOLD, 14));
		lblCreateNewAccount.setBounds(39, 71, 189, 15);
		contentPane.add(lblCreateNewAccount);
		
		JLabel lblUsername = new JLabel("Username");
		lblUsername.setFont(new Font("Dialog", Font.BOLD, 14));
		lblUsername.setBounds(160, 98, 91, 15);
		contentPane.add(lblUsername);
		
		JLabel lblFullname = new JLabel("Fullname");
		lblFullname.setFont(new Font("Dialog", Font.BOLD, 14));
		lblFullname.setBounds(160, 133, 70, 15);
		contentPane.add(lblFullname);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("Dialog", Font.BOLD, 14));
		lblPassword.setBounds(160, 168, 91, 15);
		contentPane.add(lblPassword);
		
		JLabel lblPhone = new JLabel("Phone");
		lblPhone.setFont(new Font("Dialog", Font.BOLD, 14));
		lblPhone.setBounds(160, 203, 70, 15);
		contentPane.add(lblPhone);
		
		JLabel lblEmail = new JLabel("Email");
		lblEmail.setFont(new Font("Dialog", Font.BOLD, 14));
		lblEmail.setBounds(160, 238, 70, 15);
		contentPane.add(lblEmail);
		
		tf_username = new JTextField();
		tf_username.setFont(new Font("Dialog", Font.PLAIN, 14));
		tf_username.setBounds(253, 98, 316, 19);
		contentPane.add(tf_username);
		tf_username.setColumns(10);
		
		tf_fullname = new JTextField();
		tf_fullname.setFont(new Font("Dialog", Font.PLAIN, 14));
		tf_fullname.setColumns(10);
		tf_fullname.setBounds(253, 133, 316, 19);
		contentPane.add(tf_fullname);
		
		tf_phone = new JTextField();
		tf_phone.setFont(new Font("Dialog", Font.PLAIN, 14));
		tf_phone.setColumns(10);
		tf_phone.setBounds(253, 203, 316, 19);
		contentPane.add(tf_phone);
		
		tf_email = new JTextField();
		tf_email.setFont(new Font("Dialog", Font.PLAIN, 14));
		tf_email.setColumns(10);
		tf_email.setBounds(253, 238, 316, 19);
		contentPane.add(tf_email);
		
		btnCreate = new JButton("Create");
		btnCreate.setBounds(332, 269, 117, 25);
		contentPane.add(btnCreate);
		
		JLabel lblUserList = new JLabel("User list:");
		lblUserList.setFont(new Font("Dialog", Font.BOLD, 14));
		lblUserList.setBounds(39, 330, 189, 15);
		contentPane.add(lblUserList);
		
		list = new JList<String>();
		list.setBounds(77, 367, 204, 245);
		contentPane.add(list);
		
		JLabel lblInfo = new JLabel("Info");
		lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
		lblInfo.setFont(new Font("Dialog", Font.BOLD, 14));
		lblInfo.setBounds(360, 378, 368, 15);
		contentPane.add(lblInfo);
		
		JLabel lblEmail_2 = new JLabel("Email:");
		lblEmail_2.setFont(new Font("Dialog", Font.BOLD, 14));
		lblEmail_2.setBounds(360, 515, 70, 15);
		contentPane.add(lblEmail_2);
		
		JLabel lblPhone_2 = new JLabel("Phone:");
		lblPhone_2.setFont(new Font("Dialog", Font.BOLD, 14));
		lblPhone_2.setBounds(360, 480, 70, 15);
		contentPane.add(lblPhone_2);
		
		JLabel lblFullname_2 = new JLabel("Fullname:");
		lblFullname_2.setFont(new Font("Dialog", Font.BOLD, 14));
		lblFullname_2.setBounds(360, 446, 91, 15);
		contentPane.add(lblFullname_2);
		
		JLabel lblUsername_2 = new JLabel("Username:");
		lblUsername_2.setFont(new Font("Dialog", Font.BOLD, 14));
		lblUsername_2.setBounds(360, 411, 91, 15);
		contentPane.add(lblUsername_2);
		
		btnExit = new JButton("Exit");
		btnExit.setBounds(345, 640, 117, 25);
		contentPane.add(btnExit);
		
//		lblInfo_username = new JLabel("");
//		lblInfo_username.setHorizontalAlignment(SwingConstants.CENTER);
//		lblInfo_username.setFont(new Font("Dialog", Font.BOLD, 14));
//		lblInfo_username.setBounds(455, 411, 295, 15);
//		contentPane.add(lblInfo_username);
		
		JLabel lblInfo_fullname = new JLabel("");
		lblInfo_fullname.setHorizontalAlignment(SwingConstants.CENTER);
		lblInfo_fullname.setFont(new Font("Dialog", Font.BOLD, 14));
		lblInfo_fullname.setBounds(455, 446, 295, 15);
		contentPane.add(lblInfo_fullname);
		
		JLabel lblInfo_phone = new JLabel("");
		lblInfo_phone.setHorizontalAlignment(SwingConstants.CENTER);
		lblInfo_phone.setFont(new Font("Dialog", Font.BOLD, 14));
		lblInfo_phone.setBounds(455, 480, 295, 15);
		contentPane.add(lblInfo_phone);
		
		JLabel lblInfo_email = new JLabel("");
		lblInfo_email.setHorizontalAlignment(SwingConstants.CENTER);
		lblInfo_email.setFont(new Font("Dialog", Font.BOLD, 14));
		lblInfo_email.setBounds(455, 515, 295, 15);
		contentPane.add(lblInfo_email);
		
		dlmUsers = new CustomListModel<String>(new ArrayList<String>());
		this.userList.stream().forEach(user -> {
			dlmUsers.addElement(user.getUserName());
		});
		list.setModel(dlmUsers);
		
		pf_password = new JPasswordField();
		pf_password.setFont(new Font("Dialog", Font.PLAIN, 14));
		pf_password.setBounds(253, 168, 316, 19);
		contentPane.add(pf_password);
		
		JLabel lblInfo_username = new JLabel("");
		lblInfo_username.setHorizontalAlignment(SwingConstants.CENTER);
		lblInfo_username.setFont(new Font("Dialog", Font.BOLD, 14));
		lblInfo_username.setBounds(463, 411, 295, 15);
		contentPane.add(lblInfo_username);
		
		list.addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent event) {
		        if (!event.getValueIsAdjusting()){
		            JList source = (JList)event.getSource();
		            String selected = source.getSelectedValue().toString();
		            UserDto u = client.getUserInfo(selected);
		            if(u != null) {
		            	lblInfo_username.setText(u.getUserName());
		            	lblInfo_fullname.setText(u.getFullName());
		            	lblInfo_phone.setText(u.getPhone());
		            	lblInfo_email.setText(u.getEmail());
		            }
		        }
		    }
		});
		btnCreate.addActionListener(this);
		btnExit.addActionListener(this);
	}
}
