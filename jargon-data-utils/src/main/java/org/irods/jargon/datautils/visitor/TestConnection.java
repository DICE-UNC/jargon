package org.irods.jargon.datautils.visitor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestConnection {
	
	public static void main(String args[]) throws SQLException {
	
		getConnection();
	}
	public static Connection getConnection() throws SQLException {
	 
	    try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    Connection connection = null;
	    connection = DriverManager.getConnection(
	       "jdbc:postgresql://ehsntplp01.niehs.nih.gov:5432/thunder","thunder", "thunderzzzz!");
	    //jdbc:postgresql://ehsntplp01.niehs.nih.gov:5432/thunder
	    //connection.close();
	    if (connection != null) {
			System.out.println("You made it, take control your database now!");
			
			DatabaseMetaData md = connection.getMetaData();
			ResultSet rs = md.getColumns(null, null, "search_commons", null);
					//md.getTables(null, null, "search_commons", null);
			while (rs.next()) {
			  System.out.println(rs.getString(3));
			}
			
		} else {
			System.out.println("Failed to make connection!");
		}
	    return connection;
	}
}
