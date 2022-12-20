package Server.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import Server.dto.FileDto;
import Server.models.Files;

public class FileDAL {
	PreparedStatement ps;
	public boolean changePermission(int FID, int permission) {
		String sql = "update Files set Permission = ? where FID = ?";
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql);
			ps.setInt(1, permission);
			ps.setInt(2, FID);
			ps.execute();
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public ArrayList<FileDto> getAllFiles(int parentID) {
		String sql1 = "select * from Files where ParentID = ? ";
		ArrayList<FileDto> list = new ArrayList<FileDto>();		
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql1);
			ps.setInt(1, parentID);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				FileDto f = new FileDto();
				f.setFID(rs.getInt("FID"));
				f.setParentID(rs.getInt("ParentID"));
				f.setPath(rs.getString("Path"));
				f.setName(rs.getString("Name"));
				f.setSize(rs.getLong("Size"));
				f.setLastEditedBy(rs.getString("LastEditedBy"));
				f.setOwner(rs.getString("Owner"));
				f.setPermission(rs.getInt("Permission"));
				f.setLastEditedDate(rs.getTimestamp("LastEditedDate"));
				f.setCreatedDate(rs.getTimestamp("CreatedDate"));
				f.setType(rs.getString("Type"));
				list.add(f);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return list;
	}
}
