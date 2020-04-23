package com.cptingle.MCAdminConnector;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.cptingle.MCAdminConnector.database.Connect;
import com.cptingle.MCAdminConnector.database.DBConsumer;
import com.cptingle.MCAdminConnector.database.Query;

public class Host {
	public static final int PORT = 33233;

	/**
	 * Instance Variables
	 */
	private static ServerSocket ss;
	private static Server server;
	private static ServerManager sm;
	private static Connect connection;
	private static DBConsumer dbconsumer;
	private static BlockingQueue<Query> dbQueue = new LinkedBlockingQueue<Query>();

	private static boolean isRunning;
	/**
	 * Void Main
	 */
	public static void main(String[] args) throws Exception {
		isRunning = true;
		
		
		server = new Server();
		server.start();
		
		sm = new ServerManager();
		
		server.getLogger().info("Connecting to database...");
		connection = new Connect();
		server.getLogger().info("Successfully connected to database");
		
		server.getLogger().info("Accepting Connections");
		ss = new ServerSocket(PORT);

		dbconsumer = new DBConsumer(dbQueue, connection);
		Thread dbThread = new Thread(dbconsumer);
		dbThread.start();
		dbThread.setName("Database thread");
		

		server.getLogger().info("Server started");
		server.getLogger().info("Waiting for clients to connect");
		while (isRunning) {
			Socket soc = ss.accept();
			server.getLogger().info("Connection Established with " + soc.getInetAddress());

			MCServer newServer = new MCServer(soc);
			
			sm.holdServer(newServer);
		}
	}
	
	public static Server getServer() {
		return server;
	}

	public static ServerManager getManager() {
		return sm;
	}
	
	public static BlockingQueue<Query> getDBQueue() {
		return dbQueue;
	}
	
	public static void shutDown() {
		sm.shutDown();
		isRunning = false;
	}
}
