package au.gov.nsw.rms.intg.rec_rep_test.mq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReplayMessageProgram {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("-------- Oracle JDBC Connection Testing ------");
		String payload = "";
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			
			System.out.println("Oracle JDBC Driver Registered!");
	
			Connection connection = null;
	
			try {
	
				connection = DriverManager.getConnection(
						"jdbc:oracle:thin:@localhost:1521:xe", "recordreplay",
						"recordreplay");
	
			} catch (SQLException e) {
	
				System.out.println("Connection Failed! Check output console");
				e.printStackTrace();
				return;
	
			}
	
			if (connection != null) {
				System.out.println("You made it, take control your database now!");
				
				PreparedStatement ps = connection.prepareStatement("select b.DATA from WMB_BINARY_DATA b inner join WMB_MSGS a on a.WMB_MSGKEY = b.WMB_MSGKEY where a.LOCAL_TRANSACTION_ID = ?");
				ps.setString(1, "test6788");
				ResultSet rs = ps.executeQuery();
				if(rs!=null){
					while(rs.next()){
						payload = rs.getString(1);
					}
				}
				
				try {
					connection.close();
				} catch (SQLException e) {
					System.out.println("Failed to close the connection!!");
					e.printStackTrace();
				}
			} else {
				System.out.println("Failed to make connection!");
			}
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your Oracle JDBC Driver?");
			e.printStackTrace();
			return;

		} catch (SQLException e1) {
			e1.printStackTrace();
		}

	}

}
