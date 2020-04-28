package com.cptingle.MCAdminConnector.database;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

import com.cptingle.MCAdminConnector.Host;

public class Connect implements Closeable {

	private Connection conn;

	public Connect() {

		this.conn = null;

		initializeConnection();

		Timer timer = new Timer();

		// Schedule to run after every 3 second(3000 millisecond)
		timer.schedule(new KeepDBAlive(conn), 600000, 900000);
	}

	public Connection getConnection() {
		return conn;
	}
	
	public void reconnect() {
		this.conn = null;
		initializeConnection();
	}

	private void initializeConnection() {
		while (this.conn == null) {
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
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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

	private class KeepDBAlive extends TimerTask {
		private Connection conn;

		public KeepDBAlive(Connection conn) {
			this.conn = conn;
		}

		// run is a abstract method that defines task performed at scheduled time.
		public void run() {
			try {
				Statement st = conn.createStatement();
				st.executeQuery("SELECT * FROM servers");
				Host.getServer().getLogger().info("DB Ping Sent");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
