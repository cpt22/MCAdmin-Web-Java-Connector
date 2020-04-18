package com.cptingle.MCAdminConnector.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
	private InputStream is;
	private OutputStream os;
	private ObjectOutputStream outS;
	private ObjectInputStream inS;

	private Queue<Object> incomingInvalidQueue;

	private volatile Object incomingObject;

	public MCServerSocketHandler(MCServer s) {
		sm = Host.getManager();

		server = s;

		incomingInvalidQueue = new LinkedList<Object>();

		while (socket == null || is == null || os == null || outS == null || inS == null) {
			socket = s.getSocket();
			is = s.getInputStream();
			os = s.getOutputStream();
			outS = s.getObjectOutputStream();
			inS = s.getObjectInputStream();
		}

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
	 * Called after server is validated. This processes any objects that were sent
	 * before server is validated.
	 */
	public void processAllPreValidation() {
		while (incomingInvalidQueue.peek() != null) {
			processIncoming(incomingInvalidQueue.remove());
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
