package Server.bll;

import Server.dal.UserDAL;
import Server.dto.LoginDto;

public class UserBLL {
	private UserDAL userDal;
	public UserBLL() {
		userDal = new UserDAL();
	}
	public boolean hasUserName(String username) {
		return userDal.hasUserName(username);
	}
	public boolean checkLoginInfo(String username, String password) {
		return userDal.Login(username, password);
	}
}
