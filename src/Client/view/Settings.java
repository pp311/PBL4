package Client.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;

public class Settings extends JFrame implements ActionListener{

	private JPanel contentPane;
	JRadioButton rdbtnFtpActiveMode;
	JRadioButton rdbtnFtpPassiveMode;
	JRadioButton rdbtnTftp;
	JButton btnSave;
	JButton btnCancel;
	Client client;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Settings frame = new Settings("", "", null);
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
	public Settings(String method, String mode, Client client) {
		this.client = client;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 550, 350);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		ButtonGroup G = new ButtonGroup();
		G.add(rdbtnFtpActiveMode);
		G.add(rdbtnFtpPassiveMode);
		G.add(rdbtnTftp);
		JLabel lblSettings = new JLabel("Settings");
		lblSettings.setFont(new Font("Dialog", Font.BOLD, 18));
		lblSettings.setHorizontalAlignment(SwingConstants.CENTER);
		lblSettings.setBounds(0, 25, 539, 35);
		contentPane.add(lblSettings);
		
		JLabel lblFileTransferMode = new JLabel("File transfer mode:");
		lblFileTransferMode.setBounds(28, 74, 135, 24);
		contentPane.add(lblFileTransferMode);
		
		rdbtnFtpActiveMode = new JRadioButton("FTP Active mode");
		rdbtnFtpActiveMode.setBounds(70, 106, 149, 23);
		contentPane.add(rdbtnFtpActiveMode);
		
		rdbtnFtpPassiveMode = new JRadioButton("FTP Passive mode");
		rdbtnFtpPassiveMode.setBounds(70, 145, 188, 23);
		contentPane.add(rdbtnFtpPassiveMode);
		
		rdbtnTftp = new JRadioButton("TFTP");
		rdbtnTftp.setBounds(70, 183, 149, 23);
		contentPane.add(rdbtnTftp);
		
		btnSave = new JButton("Save");
		btnSave.setBounds(91, 276, 117, 25);
		contentPane.add(btnSave);
		
		btnCancel = new JButton("Cancel");
		btnCancel.setBounds(305, 276, 117, 25);
		contentPane.add(btnCancel);
		
		G.add(rdbtnFtpActiveMode);
		G.add(rdbtnFtpPassiveMode);
		G.add(rdbtnTftp);
		
		if(method.equals("TFTP")) {
			rdbtnTftp.setSelected(true);
		}
		else {
			if(mode.equals("PORT")) {
				rdbtnFtpActiveMode.setSelected(true);
			}
			else {
				rdbtnFtpPassiveMode.setSelected(true);
			}
		}
		
		btnSave.addActionListener(this);
		btnCancel.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == btnCancel) {
			this.dispose();
		}
		else if(e.getSource() == btnSave) {
			client.defaultMethod = "FTP";
			if(rdbtnTftp.isSelected()) {
				client.defaultMethod = "TFTP";
			} else if (rdbtnFtpActiveMode.isSelected()) {
				client.defaultMode = "PORT";
			} else {
				client.defaultMode = "PASV";
			}
			this.dispose();
		}
	}
}
