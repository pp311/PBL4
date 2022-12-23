package Server.bll;

import java.util.ArrayList;

import Server.dal.UserDAL;
import Server.dto.LoginDto;
import Server.dto.UserDto;
import Server.models.User;

public class UserBLL {
	private UserDAL userDal;
	public UserBLL() {
		userDal = new UserDAL();
	}
	public boolean hasUserName(String username) {
		return userDal.hasUserName(username);
	}
	public UserDto getCurrentUserInfo(String username) {
		return userDal.getCurrentUserInfo(username);
	}
	public boolean checkLoginInfo(String username, String password) {
		return userDal.Login(username, password);
	}
	public ArrayList<UserDto> getAllUser() {
		return new UserDAL().getAllUser();
	}
	public boolean createAccount(UserDto u) {
		return new UserDAL().createNewAccount(u);
	}
	public boolean changePassword(String uname, String pass) {
		return new UserDAL().changePassword(uname, pass);
	}
}