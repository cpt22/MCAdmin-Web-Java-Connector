package com.cptingle.MCAdminConnector.database;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect implements Closeable {
	
	private static Connection conn;
	
	public Connect() {
	
		this.conn = null;
		
		initializeConnection();
	}
	
	public Connection getConnection() {
		return conn;
	}
	
	private void initializeConnection() {
		String url = "jdbc:mysql://localhost:3306/mcadmin";
        String user = "mcadmin";
        String password = "fUFgraJCouBUDGya";
        
        try {
        	conn = DriverManager.getConnection(url, user, password);
        	if (conn != null) {
        	}
        } catch (SQLException ex) {
		    System.out.println("An error occurred. Maybe user/password is invalid");
		    ex.printStackTrace();
		}
	}
	
	
	public void close() {
		if (conn != null) {
            try {
    			// TODO: Log error to server

                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
	}
}
