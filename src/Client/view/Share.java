package Client.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mysql.cj.x.protobuf.MysqlxCrud.Collection;

import Common.dto.FileDto;
import Common.dto.UserDto;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;

public class Share extends JFrame implements ActionListener, DocumentListener {

	private JPanel contentPane;
	private JTextField tfUsers;
	private JTextField tfGroups;
	private JButton btnCancel;
	private JButton btnRemoveUser;
	private JButton btnAddUser;
	private JButton btnRemoveGroup;
	private JButton btnAddGroup;
	private JButton btnSave;
	private CustomListModel<String> dlmUsers;
	private CustomListModel<String> dlmAddedUsers;
	private CustomListModel<String> dlmGroups;
	private CustomListModel<String> dlmAddedGroups;
	private List<String> lUsers = new ArrayList<String>();
	private List<String> lAddedUsers = new ArrayList<String>();
	private List<String> lGroups = new ArrayList<String>();
	private List<String> lAddedGroups = new ArrayList<String>();
	private JList<String> listUsers;
	private JList<String> listAddedUsers;
	private JList<String> listGroups;
	private JList<String> listAddedGroups;
	private JLabel lblAll;
	private JLabel lblShared;
	private JLabel lblAll_1;
	private JLabel lblShared_1;
	private JComboBox cbbAnyone;
	private int currentPermission;
	private Client client;
	private List<UserDto> userList;
	private List<UserDto> userList_Shared;
	private FileDto fileDto;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Share frame = new Share(null, null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private ArrayList<String> getUsers() {
		ArrayList<String> users = new ArrayList<>();
		for (UserDto user : userList) {
			users.add(user.getUserName());
			for (UserDto user_s : userList_Shared) {
				if(user.getUserName().equals(user_s.getUserName()))
					users.remove(user.getUserName());
			}
		}
		Collections.sort(users);
		return users;
	}
	
	private ArrayList<String> getSharedUsers() {
		ArrayList<String> users = new ArrayList<>();
			for (UserDto user_s : userList_Shared) {
					users.add(user_s.getUserName());
			}
		Collections.sort(users);
		return users;
	}
	
	private void bindData() {
		getUsers().stream().forEach(user -> {
			dlmUsers.addElement(user);
		});
		listUsers.setModel(dlmUsers);
		
		getSharedUsers().stream().forEach(user -> {
			dlmAddedUsers.addElement(user);
		});
		listAddedUsers.setModel(dlmAddedUsers);
		
		
		getGroups().stream().forEach(group -> {
			dlmGroups.addElement(group);
		});
		listGroups.setModel(dlmGroups);
		
	}
	
	private ArrayList<String> getGroups() {
		ArrayList<String> groups = new ArrayList<>();
		groups.add("Phong nhan su");
		groups.add("Phong IT");
		groups.add("Phong marketing");
		Collections.sort(groups);
		return groups;
	}
	
	
	private void searchFilterUsers(String searchTerm) {
		
		ArrayList<String> users = (ArrayList<String>)lUsers;
		CustomListModel<String> newDlm = new CustomListModel<String>(new ArrayList<String>());
		
		
		users.stream().forEach(user -> {
			if(user.toLowerCase().contains(searchTerm.toLowerCase())) {
				newDlm.addElement(user);
			}
		});
		
		dlmUsers = newDlm;
		listUsers.setModel(dlmUsers);
	}
	
	private void searchFilterGroups(String searchTerm) {
		List<String> groups = getGroups();
		CustomListModel<String> newDlm = new CustomListModel<String>(new ArrayList<String>());
		
		groups.stream().forEach(group -> {
			if(group.toLowerCase().contains(searchTerm.toLowerCase())) {
				newDlm.addElement(group);
			}
		});
		
		dlmGroups = newDlm;
		listGroups.setModel(dlmGroups);
	}
	

	/**
	 * Create the frame.
	 */
	public Share(FileDto fileDto, Client client) {
		this.currentPermission = fileDto.getPermission();
		this.client = client;
		this.userList = client.getAllUser();
		this.userList_Shared = client.getSharedUser(fileDto.getFID());
		this.fileDto = fileDto;
		
		dlmUsers = new CustomListModel<String>(lUsers);
		dlmAddedUsers = new CustomListModel<String>(lAddedUsers);
		dlmGroups = new CustomListModel<String>(lGroups);
		dlmAddedGroups = new CustomListModel<String>(lAddedGroups);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 600, 700);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblPermissionSettings = new JLabel("Permission Settings");
		lblPermissionSettings.setFont(new Font("Dialog", Font.BOLD, 18));
		lblPermissionSettings.setHorizontalAlignment(SwingConstants.CENTER);
		lblPermissionSettings.setBounds(12, 35, 582, 33);
		contentPane.add(lblPermissionSettings);
		
		JLabel lblAnyoneCan = new JLabel("Anyone can:");
		lblAnyoneCan.setBounds(22, 80, 103, 23);
		contentPane.add(lblAnyoneCan);
		
		cbbAnyone = new JComboBox();
		cbbAnyone.setModel(new DefaultComboBoxModel(new String[] {"View", "Edit"}));
		cbbAnyone.setSelectedIndex(0);
		cbbAnyone.setEditable(true);
		cbbAnyone.setBounds(152, 80, 171, 24);
		contentPane.add(cbbAnyone);
		
		JLabel lblShare = new JLabel("Share permission:");
		lblShare.setFont(new Font("Dialog", Font.BOLD, 14));
		lblShare.setBounds(22, 131, 165, 17);
		contentPane.add(lblShare);
		
		JLabel lblUsers = new JLabel("Users");
		lblUsers.setBounds(43, 174, 82, 15);
		contentPane.add(lblUsers);
		
		tfUsers = new JTextField();
		tfUsers.setBounds(143, 172, 171, 19);
		contentPane.add(tfUsers);
		tfUsers.setColumns(10);
		
		listUsers = new JList<String>();
		listUsers.setBounds(43, 223, 209, 156);
		contentPane.add(listUsers);
		
		listAddedUsers = new JList<String>();
		listAddedUsers.setBounds(337, 223, 209, 156);
		contentPane.add(listAddedUsers);
		
		btnAddUser = new JButton(">>");
		btnAddUser.setBounds(265, 266, 58, 25);
		contentPane.add(btnAddUser);
		
		btnRemoveUser = new JButton("<<");
		btnRemoveUser.setBounds(264, 311, 59, 25);
		contentPane.add(btnRemoveUser);
		
		JLabel lblGroups = new JLabel("Groups");
		lblGroups.setBounds(43, 409, 70, 15);
		contentPane.add(lblGroups);
		
		tfGroups = new JTextField();
		tfGroups.setColumns(10);
		tfGroups.setBounds(143, 407, 171, 19);
		contentPane.add(tfGroups);
		
		listGroups = new JList<String>();
		listGroups.setBounds(43, 463, 209, 156);
		contentPane.add(listGroups);
		
		listAddedGroups = new JList<String>();
		listAddedGroups.setBounds(337, 463, 209, 156);
		contentPane.add(listAddedGroups);
		
		btnAddGroup = new JButton(">>");
		btnAddGroup.setBounds(265, 490, 58, 25);
		contentPane.add(btnAddGroup);
		
		btnRemoveGroup = new JButton("<<");
		btnRemoveGroup.setBounds(264, 537, 59, 25);
		contentPane.add(btnRemoveGroup);
		
		btnSave = new JButton("Save");
		btnSave.setBounds(152, 631, 117, 25);
		contentPane.add(btnSave);
		
		btnCancel = new JButton("Cancel");
		btnCancel.setBounds(315, 631, 117, 25);
		contentPane.add(btnCancel);
		
		JComboBox cbbShare = new JComboBox();
		cbbShare.setModel(new DefaultComboBoxModel(new String[] {"View", "Edit", "Delete"}));
		cbbShare.setSelectedIndex(0);
		cbbShare.setEditable(true);
		cbbShare.setBounds(192, 127, 171, 24);
		contentPane.add(cbbShare);
		
		listUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listAddedUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listGroups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listAddedGroups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		lblAll = new JLabel("All");
		lblAll.setBounds(43, 201, 70, 15);
		contentPane.add(lblAll);
		
		lblShared = new JLabel("Shared");
		lblShared.setBounds(335, 201, 70, 15);
		contentPane.add(lblShared);
		
		lblAll_1 = new JLabel("All");
		lblAll_1.setBounds(43, 436, 70, 15);
		contentPane.add(lblAll_1);
		
		lblShared_1 = new JLabel("Shared");
		lblShared_1.setBounds(337, 436, 70, 15);
		contentPane.add(lblShared_1);
		
		btnCancel.addActionListener(this);
		btnAddUser.addActionListener(this);
		btnRemoveUser.addActionListener(this);
		btnAddGroup.addActionListener(this);
		btnRemoveGroup.addActionListener(this);
		btnSave.addActionListener(this);
		
		listGroups.setVisible(false);
		listAddedGroups.setVisible(false);
		btnAddGroup.setVisible(false);
		btnRemoveGroup.setVisible(false);
		lblGroups.setVisible(false);
		tfGroups.setVisible(false);
		lblShare.setVisible(false);
		cbbShare.setVisible(false);
		lblAll_1.setVisible(false);
		lblShared_1.setVisible(false);
		lblUsers.setVisible(false);
		tfUsers.setVisible(false);
		
		tfGroups.getDocument().addDocumentListener(this);
		tfUsers.getDocument().addDocumentListener(this);
		
		cbbAnyone.setSelectedIndex(currentPermission-1);
		bindData();
	}
	
	private void sortList(List list, CustomListModel<String> listModel) {
		if(list.size() > 0) {
			Collections.sort(list);
			listModel.fireDataChanged();			
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnCancel) {
			this.dispose();
		}
		if(e.getSource() == btnSave) {
			int anyonePermission = cbbAnyone.getSelectedIndex();
			client.setAnyonePermission(fileDto.getFID(), anyonePermission+1);
			ArrayList<Integer> uidList = new ArrayList<Integer>();
			for (String username : lAddedUsers) {
				for (UserDto user : userList) {
					if(user.getUserName().equals(username))
						uidList.add(user.getUID());
				}
			}
			client.setShare(uidList, fileDto.getFID());
			client.loadTable();
			this.dispose();
		}
		if(e.getSource() == btnAddUser) {
			String selected = listUsers.getSelectedValue();
			int index = listUsers.getSelectedIndex();
			if(selected != null) {
				dlmAddedUsers.addElement(selected);
				sortList(lAddedUsers, dlmAddedUsers);
				listAddedUsers.setModel(dlmAddedUsers);	
				
				dlmUsers.removeElementAt(index);
				sortList(lUsers, dlmUsers);
				listUsers.setModel(dlmUsers);
			}
		}
		if(e.getSource() == btnRemoveUser) {
			String selected = listAddedUsers.getSelectedValue();
			int index = listAddedUsers.getSelectedIndex();
			if(index != -1) {
				dlmAddedUsers.removeElementAt(index);
				sortList(lAddedUsers, dlmAddedUsers);
				listAddedUsers.setModel(dlmAddedUsers);
				
				dlmUsers.addElement(selected);
				sortList(lUsers, dlmUsers);
				listUsers.setModel(dlmUsers);	
			}
		}
		if(e.getSource() == btnAddGroup) {
			String selected = listGroups.getSelectedValue();
			int index = listGroups.getSelectedIndex();
			if(selected != null) {
				dlmAddedGroups.addElement(selected);
				sortList(lAddedGroups, dlmAddedGroups);
				listAddedGroups.setModel(dlmAddedGroups);	
				
				dlmGroups.removeElementAt(index);
				sortList(lGroups, dlmGroups);
				listGroups.setModel(dlmGroups);
			}
		}
		if(e.getSource() == btnRemoveGroup) {
			String selected = listAddedGroups.getSelectedValue();
			int index = listAddedGroups.getSelectedIndex();
			if(index != -1) {
				dlmAddedGroups.removeElementAt(index);
				sortList(lAddedGroups, dlmAddedGroups);
				listAddedGroups.setModel(dlmAddedGroups);
				
				dlmGroups.addElement(selected);
				sortList(lGroups, dlmGroups);
				listGroups.setModel(dlmGroups);
			}
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		if(e.getDocument() == tfUsers.getDocument()) {
			String searchTerm = tfUsers.getText();
			searchFilterUsers(searchTerm);
		}
		if(e.getDocument() == tfGroups.getDocument()) {
			String searchTerm = tfGroups.getText();
			searchFilterGroups(searchTerm);
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		if(e.getDocument() == tfUsers.getDocument()) {
			String searchTerm = tfUsers.getText();
			searchFilterUsers(searchTerm);
		}
		if(e.getDocument() == tfGroups.getDocument()) {
			String searchTerm = tfGroups.getText();
			searchFilterGroups(searchTerm);
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
		
	}
}
