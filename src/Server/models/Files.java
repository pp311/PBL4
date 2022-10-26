package Server.models;

import java.io.Serializable;
import java.util.Date;

public class Files implements Serializable {
	private static final long serialVersionUID = 1L;
	private int FID, ParentID, Permission;
	private String Name, Type, Path, Owner, LastEditedBy;
	private Date LastEditedDate, CreatedDate;
	
	public Files(int fID, int parentID, int permission, String name, String type, String path, String owner,
			String lastEditedBy, Date lastEditedDate, Date createdDate, long size) {
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
	public Date getLastEditedDate() {
		return LastEditedDate;
	}
	public void setLastEditedDate(Date lastEditedDate) {
		LastEditedDate = lastEditedDate;
	}
	public Date getCreatedDate() {
		return CreatedDate;
	}
	public void setCreatedDate(Date createdDate) {
		CreatedDate = createdDate;
	}
	public long getSize() {
		return Size;
	}
	public void setSize(long size) {
		Size = size;
	}
	private long Size;
	
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
