package Server.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import Common.dto.FileDto;
import Common.dto.UserDto;

public class ShareDAL {
	PreparedStatement ps;
	public boolean setShare(ArrayList<Integer> userList, int fid) {
		try {
			Connection db = DBConnection.getInstance().getConection();
			String sql = "update Share set Permission = 0 where FID = ?";
			ps = db.prepareStatement(sql);
			ps.setInt(1, fid);
			ps.execute();
			for (int uid : userList) {
				sql = "select * from Share where UID = ? and FID = ?";
				
					ps = db.prepareStatement(sql);
					ps.setInt(1, uid);
					ps.setInt(2, fid);
					ResultSet rs = ps.executeQuery();
					boolean isExist = false;
					if(rs.next()) {
						isExist = true;
					}
					if(!isExist) {
						sql = "insert into Share(UID, FID, Permission) values(?,?,2)";
						ps = db.prepareStatement(sql);
						ps.setInt(1, uid);
						ps.setInt(2, fid);
						ps.execute();
					} else {
						sql = "update Share set Permission = 2 where UID = ? and FID = ?";
						ps = db.prepareStatement(sql);
						ps.setInt(1, uid);
						ps.setInt(2, fid);
						ps.execute();
					}
			}
			sql = "delete from Share where Permission = 0";
			ps = db.prepareStatement(sql);
			ps.execute();
		} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		
	
		return true;
	}
	
	public ArrayList<UserDto> getSharedList(int FID) {
		String sql1 = "select Share.UID, UserName, FullName, Role from Share inner join User on Share.UID = User.UID where FID = ? ";
		ArrayList<UserDto> list = new ArrayList<UserDto>();		
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql1);
			ps.setInt(1, FID);
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
