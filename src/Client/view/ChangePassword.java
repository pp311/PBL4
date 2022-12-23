package Client.view;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JPasswordField;

public class ChangePassword extends JFrame implements ActionListener{

	private JPanel contentPane;
	private JPasswordField pf_oldpassword;
	private JPasswordField pf_newpassword;
	private JPasswordField pf_confirm;
	private Client client;
		
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChangePassword frame = new ChangePassword(null);
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
	JButton btnCancel;
	JButton btnSave;
	public ChangePassword(Client client) {
		this.client = client;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 320);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblChangePassword = new JLabel("Change Password");
		lblChangePassword.setFont(new Font("Dialog", Font.BOLD, 18));
		lblChangePassword.setHorizontalAlignment(SwingConstants.CENTER);
		lblChangePassword.setBounds(12, 34, 414, 26);
		contentPane.add(lblChangePassword);
		
		JLabel lblOldPassword = new JLabel("Old password: ");
		lblOldPassword.setFont(new Font("Dialog", Font.BOLD, 14));
		lblOldPassword.setBounds(12, 104, 120, 15);
		contentPane.add(lblOldPassword);
		
		JLabel lblNewPassword = new JLabel("New password:");
		lblNewPassword.setFont(new Font("Dialog", Font.BOLD, 14));
		lblNewPassword.setBounds(12, 151, 142, 15);
		contentPane.add(lblNewPassword);
		
		JLabel lblNewLabel = new JLabel("Confirm password:");
		lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 14));
		lblNewLabel.setBounds(12, 200, 151, 15);
		contentPane.add(lblNewLabel);
		
		btnSave = new JButton("Save");
		btnSave.setBounds(71, 244, 117, 25);
		contentPane.add(btnSave);
		
		btnCancel = new JButton("Cancel");
		btnCancel.setBounds(221, 244, 117, 25);
		contentPane.add(btnCancel);
		
		pf_oldpassword = new JPasswordField();
		pf_oldpassword.setFont(new Font("Dialog", Font.PLAIN, 14));
		pf_oldpassword.setBounds(171, 102, 232, 15);
		contentPane.add(pf_oldpassword);
		
		pf_newpassword = new JPasswordField();
		pf_newpassword.setFont(new Font("Dialog", Font.PLAIN, 14));
		pf_newpassword.setBounds(171, 149, 232, 15);
		contentPane.add(pf_newpassword);
		
		pf_confirm = new JPasswordField();
		pf_confirm.setFont(new Font("Dialog", Font.PLAIN, 14));
		pf_confirm.setBounds(171, 198, 232, 15);
		contentPane.add(pf_confirm);
		
		btnCancel.addActionListener(this);
		btnSave.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == btnCancel) {
			this.dispose();
		}
		if(e.getSource() == btnSave) {
			String old_pw = String.valueOf(pf_oldpassword.getPassword());
			String new_pw = String.valueOf(pf_newpassword.getPassword());
			String confirm = String.valueOf(pf_confirm.getPassword());
			if(old_pw.equals("") || old_pw == null ||
				new_pw.equals("") || new_pw == null ||
				confirm.equals("") || confirm == null ) {
					JOptionPane.showMessageDialog(null, "Vui lòng điền đầy đủ thông tin!");
			}
			else if(!new_pw.equals(confirm)) {
				JOptionPane.showMessageDialog(null, "Xác nhận mật khẩu không trùng khớp!");
			} 
			else if(client.checkPassword(old_pw) == false){
				JOptionPane.showMessageDialog(null, "Mật khẩu không chính xác!");
			} else {
				boolean res = client.changePassword(new_pw);
				if(res) {
					JOptionPane.showMessageDialog(null, "Đổi mật khẩu thành công!");
				} else {
					JOptionPane.showMessageDialog(null, "Đổi mật khẩu thất bại");
				}
			}
		}
	}

}
