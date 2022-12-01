package Server.bll;

import java.util.ArrayList;

import Server.dal.FileDAL;
import Server.dto.FileDto;
import Server.models.Files;

public class FileBLL {
	public ArrayList<FileDto> getAllFiles(int  parentID) {
		return new FileDAL().getAllFiles(parentID);
	}
}
