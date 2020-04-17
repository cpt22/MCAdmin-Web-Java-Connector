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
	private static Server serv;
	private static ServerManager sm;
	private static Connect connection;
	private static DBConsumer dbconsumer;
	private static BlockingQueue<Query> dbQueue = new LinkedBlockingQueue<Query>();

	/**
	 * Void Main
	 */
	public static void main(String[] args) throws Exception {

		System.out.println("Waiting for clients...");
		ss = new ServerSocket(PORT);

		serv = new Server();
		serv.start();

		sm = new ServerManager();
		connection = new Connect();
		
		dbconsumer = new DBConsumer(dbQueue, connection);
		new Thread(dbconsumer).start();

		while (true) {
			Socket soc = ss.accept();
			System.out.println("Connection Established with " + soc.getInetAddress());

			MCServer newServer = new MCServer(soc);
			
			sm.holdServer(newServer);
		}
	}

	public static ServerManager getManager() {
		return sm;
	}
	
	public static BlockingQueue<Query> getDBQueue() {
		return dbQueue;
	}
}
