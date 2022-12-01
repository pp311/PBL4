package Server.dal;
import Server.models.*;

import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.cj.xdevapi.PreparableStatement;

import Server.dto.*;

public class UploadDAL {
	PreparedStatement ps;
	
	public UploadDAL() {
		
	}
	public int parentID (Files files)
	{
		String path = files.getPath();
		path = path.substring(0, path.lastIndexOf(File.separator));
		String sql1 = "select FID from Files where Path = " + "'" + path + "'";
	
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql1);
			//ps.setString(1, files.getPath());
			//ps.setString(2, files.getName());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				int i = rs.getInt("FID");
				return i;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		return 0;
	}
	public boolean uploadFile (Files files)
	{
		//DateFormat df=new SimpleDateFormat("DD/MM/YYYY hh:mm:ss");
		
		try {	
		    Connection db = DBConnection.getInstance().getConection();
		    files.setFID(parentID(files));
			if(files.getOwner() != null && files.getOwner() != "") {
				String sql = "insert into Files(ParentID, Name, Type, Path, Size, CreatedDate, Owner, LastEditedDate, LastEditedBy, Permission) values "
						+ "( ?, ?, ?,?,?,?,?, ?, ? ,?)";	
				ps = db.prepareStatement(sql);
				ps.setInt(1, files.getParentID());
				ps.setString(2, files.getName());
				ps.setString(3, files.getType());
				ps.setString(4, files.getPath());
				ps.setLong(5, files.getSize());
				ps.setTimestamp(6, files.getCreatedDate());
				ps.setString(7, files.getOwner());
				ps.setTimestamp(8, files.getLastEditedDate());
				ps.setString(9, files.getLastEditedBy());
				ps.setInt(10, files.getPermission());
			}
			else {
				String sql = "update Files set Size = ?, LastEditedDate = ?, LastEditedBy = ? where Name = ? and Path = ? ";	
				ps = db.prepareStatement(sql);
				
				ps.setLong(1, files.getSize());
				
				ps.setTimestamp(2, files.getLastEditedDate());
				
				ps.setString(3, files.getLastEditedBy());
				ps.setString(4, files.getName());
				ps.setString(5, files.getPath());
			
			}
			ps.execute();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean checkFileExists(Files files )
	{
		String sql = "select * from Files where Name = ? and Path = ? ";
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql);
			ps.setString(1, files.getName());
			ps.setString(2, files.getPath());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		return false;
	}
//	public boolean delDir(Files files)
//	{
//		String sql = "delete from Files where Path = ? and Name = ?";
//		try {
//			Connection db = DBConnection.getInstance().getConection();
//			ps = db.prepareStatement(sql);
//			ps.setString(1, files.getPath());
//			ps.setString(2, files.getName());
//			ps.execute();
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}
	public boolean delFile(Files files)
	{ 
		try {
			String sql;
			Connection db = DBConnection.getInstance().getConection();
			if(files.getType() == "Dir") {
				sql = "delete from Files where Path like ?";
				ps = db.prepareStatement(sql);
				ps.setString(1, files.getPath() + "%");
			}
			else {
				sql = "delete from Files where Path = ? and Name = ?";
				ps = db.prepareStatement(sql);
				ps.setString(1, files.getPath());
				ps.setString(2, files.getName());
			}
			ps.execute();
		}		
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public int findFID(Files files)
	{
		String sql1 = "select FID from Files where Name = ? and Path = ? ";		
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql1);
			String path = files.getPath();
			path = path.substring(0, path.indexOf("\\"));
			ps.setString(1, files.getName());
			ps.setString(2, files.getPath());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				return rs.getInt("FID");
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		return 0;
	}
	public boolean deleteFile(Files files)
	{
		String sql = "delete from Files where FID = ?";
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql);
			ps.setInt(1, files.getFID());
			ps.execute();
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}
