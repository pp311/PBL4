package Server.models;

public class Group {
	private int GID;
	private String GroupName, Owner;
	public Group(int gID, String groupName, String owner) {
		super();
		GID = gID;
		GroupName = groupName;
		Owner = owner;
	}
	
	public Group() {}

	public int getGID() {
		return GID;
	}

	public void setGID(int gID) {
		GID = gID;
	}

	public String getGroupName() {
		return GroupName;
	}

	public void setGroupName(String groupName) {
		GroupName = groupName;
	}

	public String getOwner() {
		return Owner;
	}

	public void setOwner(String owner) {
		Owner = owner;
	}
	
	
	
	
}
