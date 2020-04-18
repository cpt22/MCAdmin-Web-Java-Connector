package com.cptingle.MCAdminConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.cptingle.MCAdminConnector.database.LogQuery;
import com.cptingle.MCAdminConnector.database.Query;
import com.cptingle.MCAdminConnector.database.QueryResult;
import com.cptingle.MCAdminConnector.database.QueryType;
import com.cptingle.MCAdminConnector.network.MCServerSocketHandler;
import com.cptingle.MCAdminItems.BanRequest;
import com.cptingle.MCAdminItems.KickRequest;
import com.cptingle.MCAdminItems.PlayerUpdate;
import com.cptingle.MCAdminItems.SimpleRequest;
import com.cptingle.MCAdminItems.Token;

public class MCServer {

	private String name;
	private String token;
	private String ID;
	private boolean validated;

	// Network Stuff
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private ObjectOutputStream outS;
	private ObjectInputStream inS;

	private final BlockingQueue<QueryResult> resultQueue;
	private ResultProcessor rp;
	private MCServerSocketHandler socketHandler;

	public MCServer(Socket s) {
		this.socket = s;
		resultQueue = new LinkedBlockingQueue<QueryResult>();

		this.validated = false;

		rp = new ResultProcessor(this, resultQueue);
		new Thread(rp).start();

		loadStreams();

		socketHandler = new MCServerSocketHandler(this);
	}

	public String getToken() {
		return token;
	}

	public String getID() {
		return ID;
	}

	public String getName() {
		return name;
	}

	public BlockingQueue<QueryResult> getResultQueue() {
		return resultQueue;
	}

	public MCServerSocketHandler getSocketHandler() {
		return socketHandler;
	}

	public boolean isValidated() {
		return validated;
	}

	public void setValidated(boolean val) {
		validated = val;
		if (validated) {
			Host.getManager().transferServer(this);
		}
		System.out.println("isValidated:" + validated);
	}

	/**
	 * Methods
	 */
	private void loadStreams() {
		try {
			os = socket.getOutputStream();
			outS = new ObjectOutputStream(os);
			is = socket.getInputStream();
			inS = new ObjectInputStream(is);

			send(SimpleRequest.SEND_TOKEN);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("error");
		}
	}

	/**
	 * NEtwork Stuff
	 */
	public Socket getSocket() {
		return socket;
	}

	public InputStream getInputStream() {
		return is;
	}

	public OutputStream getOutputStream() {
		return os;
	}

	public ObjectOutputStream getObjectOutputStream() {
		return outS;
	}

	public ObjectInputStream getObjectInputStream() {
		return inS;
	}

	/**
	 * Object Processing
	 */

	/**
	 * Verifies that a server's provided token is valid
	 * 
	 * @param t
	 * @return
	 */
	public boolean processToken(Token t) {
		String sql = "SELECT * FROM servers WHERE token=?";
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(t.token);
		return Host.getDBQueue().offer(new Query(this, sql, params, QueryType.VERIFY_TOKEN));
	}

	/**
	 * Process a player update which updates the database with the players current
	 * status
	 * 
	 * @param p
	 * @return
	 */
	public boolean processPlayerUpdate(PlayerUpdate p) {
		String sql = "INSERT INTO player_status (uuid, username, status, server_ID) VALUES (?,?,?," + ID
				+ ") ON DUPLICATE KEY UPDATE status=?";
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(p.uuid);
		params.add(p.username);
		params.add(p.status);
		params.add(p.status);

		return Host.getDBQueue().offer(new Query(this, sql, params, QueryType.PLAYER_STATUS_UPDATE));
	}

	public boolean processPlayerKick(KickRequest p) {
		return Host.getDBQueue().offer(new LogQuery(this, p.executor, "kick",
				"{'username':'" + p.username + "' 'uuid':'" + p.uuid + "' 'reason':'" + p.reason + "'}"));
	}

	public boolean processPlayerBan(BanRequest p) {
		String sql;
		ArrayList<Object> params = new ArrayList<Object>();
		if (p.state) {
			sql = "INSERT INTO player_banlist (uuid, username, executor, reason, server_ID) VALUES (?,?,?,?" + ID
					+ ") ON DUPLICATE KEY UPDATE username=?";
			params.add(p.uuid);
			params.add(p.username);
			params.add(p.executor);
			params.add(p.reason);
			params.add(p.username);
		} else {
			sql = "DELETE FROM player_banlist WHERE uuid=? AND server_ID=" + ID;
			params.add(p.uuid);
		}
		
		Host.getDBQueue().offer(new LogQuery(this, p.executor, "ban", "{'username':'" + p.username + "' 'uuid':'"
				+ p.uuid + "' 'reason':'" + p.reason + "' 'state':'" + p.state + "'}"));
		return Host.getDBQueue().offer(new Query(this, sql, params, QueryType.BAN));
	}

	/**
	 * Sets status to false (offline) for all players on a given server
	 * 
	 * @return
	 */
	public boolean clearOnlinePlayers() {
		String sql = "UPDATE player_status SET status=0 WHERE serverID=" + ID;
		return Host.getDBQueue().offer(new Query(this, sql, null, QueryType.PLAYER_STATUS_UPDATE));
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
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void writeObj(Object obj) throws IOException {
		outS.reset();
		outS.writeObject(obj);
		outS.flush();
	}

	/**
	 * Transfers query result into the resultQueue which stores result arrays
	 * waiting to be processed
	 * 
	 * @param qr
	 */
	public void processQueryResult(QueryResult qr) {
		try {
			resultQueue.put(qr);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close() {
		rp.notifyClose();
	}

	/**
	 * 
	 * @author Christian Tingle This class does the heavy lifting behind processing
	 *         db query results
	 */
	private class ResultProcessor implements Runnable {
		private MCServer server;
		private final BlockingQueue<QueryResult> resultQueue;
		private boolean shouldRun = true;

		public ResultProcessor(MCServer server, BlockingQueue<QueryResult> resultQueue) {
			this.server = server;
			this.resultQueue = resultQueue;
		}

		@Override
		public void run() {
			try {
				while (shouldRun) {
					consume(resultQueue.take());
				}
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}

		private void consume(QueryResult qr) {
			ArrayList<Map<String, Object>> rs = qr.result;
			if (rs.size() == 0) {
				switch (qr.type) {
				case VERIFY_TOKEN:
					server.send(SimpleRequest.INVALID_TOKEN);
					break;

				}
			}
			for (int i = 0; i < rs.size(); i++) {
				Map<String, Object> row = rs.get(i);
				switch (qr.type) {
				case VERIFY_TOKEN:

					ID = (String) row.get("ID");
					name = (String) row.get("name");
					if (ID.equals("")) {
						server.setValidated(true);
					}
					break;

				}
			}
		}
		
		public void notifyClose() {
			shouldRun = false;
		}
	}
}
