package com.cptingle.MCAdminConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.cptingle.MCAdminConnector.database.Query;
import com.cptingle.MCAdminConnector.database.QueryResult;
import com.cptingle.MCAdminConnector.database.QueryType;
import com.cptingle.MCAdminItems.PlayerUpdate;
import com.cptingle.MCAdminItems.SimpleRequest;
import com.cptingle.MCAdminItems.Token;

public class MCServer {

	private String name;
	private String token;
	private int ID;
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
	
	public int getID() {
		return ID;
	}
	
	public String getName() {
		return name;
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
	 *
	 */
	public boolean processToken(Token t) {
		String sql = "SELECT * FROM servers WHERE token=?";
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(t.token);
		return Host.getDBQueue().offer(new Query(this, sql, params, QueryType.VERIFY_TOKEN));
	}

	public boolean processPlayerUpdate(PlayerUpdate p) {
		String sql = "INSERT INTO player_status (uuid, username, status, serverID) VALUES (?,?,?," + ID
				+ ") ON DUPLICATE KEY UPDATE status=?";
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(p.uuid);
		params.add(p.username);
		params.add(p.status);
		params.add(p.status);

		return Host.getDBQueue().offer(new Query(this, sql, params, QueryType.PLAYER_STATUS_UPDATE));
	}
	
	public boolean clearOnlinePlayers() {
		String sql = "UPDATE player_status SET status=0 WHERE serverID=" + ID;
		return Host.getDBQueue().offer(new Query(this, sql, null, QueryType.PLAYER_STATUS_UPDATE));
	}

	public boolean send(Object o) {
		try {
			writeObj(o);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public BlockingQueue<QueryResult> getResultQueue() {
		return resultQueue;
	}

	public void processQueryResult(QueryResult qr) {
		try {
			resultQueue.put(qr);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeObj(Object obj) throws IOException {
		outS.reset();
		outS.writeObject(obj);
		outS.flush();
	}

	private class ResultProcessor implements Runnable {
		private MCServer server;
		private final BlockingQueue<QueryResult> resultQueue;

		public ResultProcessor(MCServer server, BlockingQueue<QueryResult> resultQueue) {
			this.server = server;
			this.resultQueue = resultQueue;
		}

		@Override
		public void run() {
			try {
				while (true) {
					consume(resultQueue.take());
				}
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}

		void consume(QueryResult qr) {
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
					int sID = (int) row.get("ID");
					name = (String) row.get("name");
					if (sID != 0) {
						server.setValidated(true);
					}
					break;

				}
			}
		}
	}
}
