package Server.models;

public class Share {
	private int FID, UID, Permission;

	public Share(int fID, int uID, int permission) {
		super();
		FID = fID;
		UID = uID;
		Permission = permission;
	}
	
	public Share() {}

	public int getFID() {
		return FID;
	}

	public void setFID(int fID) {
		FID = fID;
	}

	public int getUID() {
		return UID;
	}

	public void setUID(int uID) {
		UID = uID;
	}

	public int getPermission() {
		return Permission;
	}

	public void setPermission(int permission) {
		Permission = permission;
	}
	
	
	
	
}
