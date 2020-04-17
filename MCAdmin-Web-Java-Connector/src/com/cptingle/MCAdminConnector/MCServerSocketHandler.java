package com.cptingle.MCAdminConnector;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

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
			} catch (EOFException e) {
				close();
				break;
			} catch (Exception ex) {
				// System.out.println("Player Disconnected From: " + player.getIpAddress() + "
				// and was in game " + player.getGameID());
				ex.printStackTrace();
				break;
			}
		}
	}

	public void processAllPreValidation() {
		while (incomingInvalidQueue.peek() != null) {
			processIncoming(incomingInvalidQueue.remove());
		}
	}

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
		}
	}

	// Closes socket related things
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {

		}
	}

}
