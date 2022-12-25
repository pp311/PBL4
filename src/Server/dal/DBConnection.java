package Server.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	private String user = "root";
	private String pass = "";
	private String hostName = "localhost";
	private String dbName = "FTP";
//	private String connectionURL = "jdbc:mysql://" + hostName + ":3306/" + dbName;
	private String connectionURL = "jdbc:mysql://" + hostName + ":3307/" + dbName;
	private Connection conn;
	private static DBConnection instance;
	
	public static DBConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DBConnection();
        }
        else if(instance.conn.isClosed()) {
        	instance = new DBConnection();
        }
        return instance;
    }
	
	 private DBConnection() {
	        connectToMySQL();
	    }
	
	public synchronized Connection getConection() {
		return conn;
	} 
	
	public void disconnect() throws SQLException{
        conn.close();
    }

   
    
    private void connectToMySQL() {
    	try {
    		Class.forName("com.mysql.cj.jdbc.Driver");
    		conn = DriverManager.getConnection(connectionURL, user, pass);
    		System.out.println("Ket noi CSDL thanh cong");
    	}
    	catch (Exception e) {
    		System.out.println("Ket noi CSDL that bai!!!");
		}
    }
}