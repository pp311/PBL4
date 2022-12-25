package Server.dal;
import Server.models.*;

import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
		String sql1 = "select FID from Files where Path = ?";
	
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql1);
			ps.setString(1, path);
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
	
	public int parentID (String path)
	{
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
	public List<String> getAllOwner (Files files){
		List<String>list = new ArrayList<String>();
		String sql = "with recursive cte (FID, ParentID, Path, Owner) as ( select FID, ParentID, Path, Owner from Files where FID = ? union all select p.FID, p.ParentID, p.Path, p.Owner from Files p inner join cte on p.FID = cte.ParentID ) select Owner from cte";		
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql);
			int fid = files.getFID();
			ps.setInt(1, fid);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				list.add(rs.getString("Owner"));
			}
			return list;
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return list;
		}
		
	}
//	public int findFID(Files files)
//	{
//		String sql1 = "select FID from Files where Name = ? and Path = ? ";		
//		try {
//			Connection db = DBConnection.getInstance().getConection();
//			ps = db.prepareStatement(sql1);
//			String path = files.getPath();
//			path = path.substring(0, path.lastIndexOf(File.separator));
//			//path = path.substring(0, path.indexOf("\\"));
//			ps.setString(1, files.getName());
//			ps.setString(2, files.getPath());
//			ResultSet rs = ps.executeQuery();
//			if (rs.next())
//			{
//				return rs.getInt("FID");
//			}
//			
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return 0;
//		}
//		return 0;
//	}
	public int findFID(Files files)
	{
		String sql1 = "select FID from Files where Path = ? ";		
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql1);
			String path = files.getPath();
			ps.setString(1, files.getPath());
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
	public List<String> getAllShared (Files files){
		List<String>list = new ArrayList<String>();
		String sql = "SELECT * FROM Share INNER JOIN User ON Share.UID=User.UID WHERE Permission = 2 AND FID = ?";		
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql);
			int fid = files.getFID();
			ps.setInt(1, fid);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				list.add(rs.getString("UserName"));
			}
			return list;
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return list;
		}
		
	}
	public List<Integer> getAllFID (Files files){
		List<Integer>list = new ArrayList<Integer>();
		String sql = "with recursive cte (FID, ParentID, Path, Owner) as ( select FID, ParentID, Path, Owner from Files where FID = ? union all select p.FID, p.ParentID, p.Path, p.Owner from Files p inner join cte on p.FID = cte.ParentID ) select FID from cte";		
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql);
			int fid = files.getFID();
			ps.setInt(1, fid);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				list.add(rs.getInt("FID"));
			}
			return list;
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return list;
		}
		
	}
	public List<String> getAllSharedByFID (int fid){
		List<String>list = new ArrayList<String>();
		String sql = "SELECT * FROM Share INNER JOIN User ON Share.UID=User.UID WHERE Permission = 2 AND FID = ?";		
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql);
			ps.setInt(1, fid);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				list.add(rs.getString("UserName"));
			}
			return list;
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return list;
		}
		
	}
	public int getPermission(Files file) {
		// TODO Auto-generated method stub
		String path = file.getPath();
		String sql1 = "select Permission from Files where Path = ?";
	
		try {
			Connection db = DBConnection.getInstance().getConection();
			ps = db.prepareStatement(sql1);
			ps.setString(1, path);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				int i = rs.getInt("Permission");
				return i;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		return 0;
	}
	public boolean uploadFolder (java.sql.Timestamp day,String editBy,int FID)
	{
		//DateFormat df=new SimpleDateFormat("DD/MM/YYYY hh:mm:ss");
		
		try {	
		    Connection db = DBConnection.getInstance().getConection();
		    
				String sql = "update Files set LastEditedDate = ?, LastEditedBy = ? where FID = ? ";	
				ps = db.prepareStatement(sql);
				
				
				
				ps.setTimestamp(1, day);
				
				
				ps.setString(2, editBy);
				ps.setInt(3, FID);
			ps.execute();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
