package com.cptingle.MCAdminConnector;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.cptingle.MCAdminConnector.database.LogQuery;
import com.cptingle.MCAdminConnector.database.Query;
import com.cptingle.MCAdminConnector.database.QueryResult;
import com.cptingle.MCAdminConnector.database.QueryType;
import com.cptingle.MCAdminConnector.network.MCServerSocketHandler;
import com.cptingle.MCAdminConnector.web.WebInterface;
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

	private final BlockingQueue<QueryResult> resultQueue;
	private ResultProcessor rp;
	private MCServerSocketHandler socketHandler;

	public MCServer(Socket s) {
		this.resultQueue = new LinkedBlockingQueue<QueryResult>();

		this.validated = false;

		this.rp = new ResultProcessor(this, resultQueue);
		Thread rpThread = new Thread(rp);
		rpThread.start();

		socketHandler = new MCServerSocketHandler(this, s);
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
			socketHandler.setName("Socket Handler " + name + " thread");
			socketHandler.processAllPreValidation();
			Host.getManager().transferServer(this);
		}
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
		createPlayer(p);
		linkPlayerAndServer(p);

		String sql = "INSERT INTO player_status (uuid, status, server_ID) VALUES (?,?,?) ON DUPLICATE KEY UPDATE status=?";
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(p.uuid);
		params.add(p.status);
		params.add(ID);
		params.add(p.status);

		try {
			Map<Object, Object> pms = new HashMap<>();
			pms.put("uuid", p.uuid);
			pms.put("serverID", this.ID);
			WebInterface.sendPost("http://192.168.0.44:8008/sendEvent", pms);
		} catch (Exception e) {

			e.printStackTrace();
		}

		return Host.getDBQueue().offer(new Query(this, sql, params, QueryType.PLAYER_STATUS_UPDATE));
	}

	public boolean createPlayer(PlayerUpdate p) {
		String sql = "INSERT INTO players (username, uuid) VALUES (?,?) ON DUPLICATE KEY UPDATE username=?";
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(p.username);
		params.add(p.uuid);
		params.add(p.username);
		return Host.getDBQueue().offer(new Query(this, sql, params, QueryType.PLAYER_STATUS_UPDATE));
	}

	public boolean linkPlayerAndServer(PlayerUpdate p) {
		String sql = "INSERT INTO player_server (uuid, server_ID) VALUES (?,?) ON DUPLICATE KEY UPDATE uuid=uuid";
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(p.uuid);
		params.add(ID);
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
			sql = "INSERT INTO player_banlist (uuid, executor, reason, server_ID) VALUES (?,?,?,?)";
			params.add(p.uuid);
			params.add(p.executor);
			params.add(p.reason);
			params.add(ID);
		} else {
			sql = "DELETE FROM player_banlist WHERE uuid=? AND server_ID=?";
			params.add(p.uuid);
			params.add(ID);
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
		String sql = "UPDATE player_status SET status=0 WHERE server_ID=?";
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(ID);
		return Host.getDBQueue().offer(new Query(this, sql, params, QueryType.PLAYER_STATUS_UPDATE));
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

	public void send(Object o) {
		socketHandler.send(o);
	}

	public boolean equals(Object o) {
		if (o instanceof MCServer) {
			return o == this;
		} else if (o instanceof String) {
			return this.getID().equals((String) o);
		}
		return false;
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
					socketHandler.send(SimpleRequest.INVALID_TOKEN);
					break;
				default:
					break;

				}
			}
			for (int i = 0; i < rs.size(); i++) {
				Map<String, Object> row = rs.get(i);
				switch (qr.type) {
				case VERIFY_TOKEN:

					ID = (String) row.get("ID");
					name = (String) row.get("name");
					if (!ID.equals("")) {
						Thread.currentThread().setName("Server " + name + " thread");
						server.setValidated(true);
						socketHandler.send(SimpleRequest.SERVER_VALIDATED);
						Host.getServer().getLogger().info("Server " + name + " validated");
					}
					break;
				default:
					break;

				}
			}
		}

		public void notifyClose() {
			shouldRun = false;
		}
	}

}
