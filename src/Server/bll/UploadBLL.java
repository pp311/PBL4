package Server.bll;

import Server.dal.UploadDAL;
import Server.dto.FileDto;
import Server.models.*;

public class UploadBLL {
	private UploadDAL uploadDAL;
	public UploadBLL() {
		uploadDAL = new UploadDAL();
	}

	public boolean uploadFile (FileDto fDto)
	{
		Files file = new Files();
		file.setPermission(2);
		file.setName(fDto.getName());
		file.setType(fDto.getType());
		file.setPath(fDto.getPath());
		file.setOwner(fDto.getOwner());
		file.setLastEditedBy(fDto.getLastEditedBy()); 
		file.setLastEditedDate(new java.sql.Timestamp(fDto.getLastEditedDate().getTime()));
		file.setCreatedDate(new java.sql.Timestamp(fDto.getCreatedDate().getTime()));
		file.setSize(fDto.getSize());
		return uploadDAL.uploadFile(file);
	}
	public boolean checkFileExists (FileDto fDto)
	{
		Files file = new Files();
		file.setName(fDto.getName());
		file.setPath(fDto.getPath());
		return uploadDAL.checkFileExists(file);
	}
	public int parentID (FileDto fDto)
	{
		Files file = new Files();
		file.setPath(fDto.getPath());
		return uploadDAL.parentID(file);
	}
	public boolean delFile (FileDto fDto)
	{
		Files file = new Files();
		file.setName(fDto.getName());
		file.setPath(fDto.getPath());
		file.setType(fDto.getType());
		return uploadDAL.delFile(file);
	}
}
