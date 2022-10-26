package Server.models;

public class User {
	private int UID;
	private String UserName, FullName, Email, Phone, Password, Role;
	public User(int uID, String userName, String fullName, String email, String phone, String password, String role) {
		super();
		UID = uID;
		UserName = userName;
		FullName = fullName;
		Email = email;
		Phone = phone;
		Password = password;
		Role = role;
	}
	
	public User() {}

	public int getUID() {
		return UID;
	}

	public void setUID(int uID) {
		UID = uID;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public String getFullName() {
		return FullName;
	}

	public void setFullName(String fullName) {
		FullName = fullName;
	}

	public String getEmail() {
		return Email;
	}

	public void setEmail(String email) {
		Email = email;
	}

	public String getPhone() {
		return Phone;
	}

	public void setPhone(String phone) {
		Phone = phone;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

	public String getRole() {
		return Role;
	}

	public void setRole(String role) {
		Role = role;
	}
	
	
	
}
