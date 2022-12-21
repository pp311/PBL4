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
			sql = "UPDATE Files SET Permission = ? WHERE FID IN\n"
					+ "(\n"
					+ "WITH RECURSIVE mgmt_levels AS ( SELECT FID, ParentID FROM Files WHERE ParentID = ? \n"
					+ "UNION ALL\n"
					+ "SELECT p.FID, p.ParentID FROM Files p INNER JOIN mgmt_levels ml ON ml.FID = p.ParentID )\n"
					+ "SELECT FID\n"
					+ "FROM mgmt_levels ml\n"
					+ ");";
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
	
	public ArrayList<FileDto> getAllFiles(int parentID, int UID) {
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
				sql1 = "select * from Share where FID = ? and UID = ?";
				ps = db.prepareStatement(sql1);
				ps.setInt(1, f.getFID());
				ps.setInt(2, UID);
				ResultSet rs1 = ps.executeQuery();
				if(rs1.next()) {
					f.setShared(true);
				}
				else {
					f.setShared(false);
				}
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
