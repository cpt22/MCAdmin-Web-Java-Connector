package com.cptingle.MCAdminConnector.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import com.cptingle.MCAdminConnector.Host;
import com.cptingle.MCAdminConnector.MCServer;
import com.cptingle.MCAdminConnector.ServerManager;
import com.cptingle.MCAdminItems.BanRequest;
import com.cptingle.MCAdminItems.KickRequest;
import com.cptingle.MCAdminItems.PlayerUpdate;
import com.cptingle.MCAdminItems.SimpleRequest;
import com.cptingle.MCAdminItems.Token;

public class MCServerSocketHandler extends Thread {
	/**
	 * Instance Variables
	 */
	private ServerManager sm;

	private MCServer server;

	private Socket socket;
	private ObjectOutputStream outS;
	private ObjectInputStream inS;

	private Queue<Object> incomingInvalidQueue;

	private volatile Object incomingObject;

	public MCServerSocketHandler(MCServer s, Socket socket) {
		this.sm = Host.getManager();

		this.server = s;
		this.socket = socket;

		this.incomingInvalidQueue = new LinkedList<Object>();
		
		loadStreams();

		this.start();
	}

	public void run() {
		while (true) {
			try {
				incomingObject = inS.readObject();
				processIncoming(incomingObject);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				close();
				break;
			}
		}
	}
	
	/**
	 * Loads all streams from socket
	 */
	private void loadStreams() {
		try {
			outS = new ObjectOutputStream(socket.getOutputStream());
			inS = new ObjectInputStream(socket.getInputStream());

			send(SimpleRequest.SEND_TOKEN);
		} catch (IOException e) {
			Host.getServer().getLogger().severe("Error loading streams");
		}
	}
	
	/**
	 * Sends provided object through the ObjectOutputStream to the client server
	 * 
	 * @param o
	 * @return
	 */
	public boolean send(Object o) {
		try {
			writeObj(o);
			System.out.println("sent: " + o.toString());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void writeObj(Object obj) throws IOException {
		outS.writeObject(obj);
		outS.flush();
	}

	/**
	 * Called after server is validated. This processes any objects that were sent
	 * before server is validated.
	 */
	public void processAllPreValidation() {
		while (incomingInvalidQueue.peek() != null) {
			processIncoming(incomingInvalidQueue.poll());
		}
	}

	/**
	 * Processing objects incoming from ObjectInputStream
	 * 
	 * @param incoming
	 */
	public void processIncoming(Object incoming) {
		if (incoming instanceof Token) {
			server.processToken((Token) incoming);
			return;
		}

		if (!server.isValidated()) {
			incomingInvalidQueue.add(incoming);
			return;
		}

		if (incoming instanceof SimpleRequest) {
			switch ((SimpleRequest) incoming) {
			case CLEAR_ONLINE_PLAYERS:
				server.clearOnlinePlayers();
				break;
			default:
				break;
			}
		} else if (incoming instanceof PlayerUpdate) {
			server.processPlayerUpdate((PlayerUpdate) incoming);
		} else if (incoming instanceof KickRequest) {
			server.processPlayerKick((KickRequest) incoming);
		} else if (incoming instanceof BanRequest) {
			server.processPlayerBan((BanRequest) incoming);
		}
	}

	// Closes socket related things
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {

		}
		
		server.close();
		
		sm.removeServer(server);
	}

}
