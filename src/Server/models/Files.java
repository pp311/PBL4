package Server.models;

import java.io.Serializable;
import java.util.Date;

public class Files implements Serializable {
	private static final long serialVersionUID = 1L;
	private int FID, ParentID, Permission;
	private String Name, Type, Path, Owner, LastEditedBy;
	private java.sql.Timestamp LastEditedDate, CreatedDate;
	private long Size;
	public Files(int fID, int parentID, int permission, String name, String type, String path, String owner,
			String lastEditedBy, java.sql.Timestamp lastEditedDate, java.sql.Timestamp createdDate, long size) {
		super();
		FID = fID;
		ParentID = parentID;
		Permission = permission;
		Name = name;
		Type = type;
		Path = path;
		Owner = owner;
		LastEditedBy = lastEditedBy;
		LastEditedDate = lastEditedDate;
		CreatedDate = createdDate;
		Size = size;
	}
	
	public Files( ) {
		
	}
	
	public int getPermission() {
		return Permission;
	}
	public void setPermission(int permission) {
		Permission = permission;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public String getPath() {
		return Path;
	}
	public void setPath(String path) {
		Path = path;
	}
	public String getOwner() {
		return Owner;
	}
	public void setOwner(String owner) {
		Owner = owner;
	}
	public String getLastEditedBy() {
		return LastEditedBy;
	}
	public void setLastEditedBy(String lastEditedBy) {
		LastEditedBy = lastEditedBy;
	}
	public java.sql.Timestamp getLastEditedDate() {
		return LastEditedDate;
	}
	public void setLastEditedDate(java.sql.Timestamp lastEditedDate) {
		LastEditedDate = lastEditedDate;
	}
	public java.sql.Timestamp getCreatedDate() {
		return CreatedDate;
	}
	public void setCreatedDate(java.sql.Timestamp createdDate) {
		CreatedDate = createdDate;
	}
	public long getSize() {
		return Size;
	}
	public void setSize(long size) {
		Size = size;
	}

	
	public int getFID() {
		return FID;
	}
	public void setFID(int fID) {
		FID = fID;
	}
	public int getParentID() {
		return ParentID;
	}
	public void setParentID(int parentID) {
		ParentID = parentID;
	}
	
}