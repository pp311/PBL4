package Server.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.cj.xdevapi.PreparableStatement;

import Server.dto.*;
import Server.models.User;


public class UserDAL {
	PreparedStatement ps;
	
	public UserDAL() {
		
	}
	
	public boolean Login(String UserName, String Password) {
		try {
			String sql = "select * from User where UserName = ? and Password = ?";
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql);
			ps.setString(1, UserName);
			ps.setString(2, Password);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			System.out.println("Co loi xay ra khi dang nhap");
			return false;
		}
		return false;
	}
	public UserDto getCurrentUserInfo(String userName) {
		String sql = "select * from User where UserName = ?";
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql);
			ps.setString(1, userName);
			ResultSet rs = ps.executeQuery();
			UserDto userDto = new UserDto();
			if(rs.next()) {
				userDto.setRole(rs.getString("Role"));
				userDto.setUID(rs.getInt("UID"));
				userDto.setFullName(rs.getString("FullName"));
				userDto.setEmail(rs.getString("Email"));
				userDto.setPhone(rs.getString("Phone"));
				userDto.setUserName(rs.getString("UserName"));
				return userDto;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public boolean hasUserName(String userName) {
		String sql = "select UID from User where UserName = ?";
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql);
			ps.setString(1, userName);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return true;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	public ArrayList<UserDto> getAllUser() {
		String sql1 = "select * from User ";
		ArrayList<UserDto> list = new ArrayList<UserDto>();		
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql1);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				UserDto u = new UserDto();
				u.setUID(rs.getInt("UID"));
				u.setUserName(rs.getString("UserName"));
				u.setFullName(rs.getString("FullName"));
				u.setRole(rs.getString("Role"));
				if(u.getRole().equals("user"))
					list.add(u);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return list;
	}
}
