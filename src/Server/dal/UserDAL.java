package Server.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.cj.xdevapi.PreparableStatement;

import Server.dto.*;


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
}
