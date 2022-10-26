package Server.models;

public class User_Group {
	private int UID, GID;

	public User_Group(int uID, int gID) {
		super();
		UID = uID;
		GID = gID;
	}
	
	public User_Group() {}

	public int getUID() {
		return UID;
	}

	public void setUID(int uID) {
		UID = uID;
	}

	public int getGID() {
		return GID;
	}

	public void setGID(int gID) {
		GID = gID;
	}
	
	
}
