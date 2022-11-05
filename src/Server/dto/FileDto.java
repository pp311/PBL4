package Server.dto;

import java.io.Serializable;
import java.util.Date;

public class FileDto implements Serializable {
	private static final long serialVersionUID = 1L;
	private int FID, ParentID, Permission;
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
	public int getPermission() {
		return Permission;
	}
	public void setPermission(int permission) {
		Permission = permission;
	}
	private String Name, Type, Path, Owner, LastEditedBy;
	private Date LastEditedDate, CreatedDate;
	private long Size;
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

}