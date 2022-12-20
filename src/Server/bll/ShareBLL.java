package Server.bll;

import java.util.ArrayList;

import Server.dal.ShareDAL;
import Server.dto.UserDto;

public class ShareBLL {
	public boolean setShare(ArrayList<Integer> userList, int fid) {
		return new ShareDAL().setShare(userList, fid);
	}
	public ArrayList<UserDto> getSharedList(int FID) {
		return new ShareDAL().getSharedList(FID);
	}
}
