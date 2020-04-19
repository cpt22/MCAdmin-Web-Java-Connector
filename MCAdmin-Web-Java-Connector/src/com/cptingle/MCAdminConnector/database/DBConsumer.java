package com.cptingle.MCAdminConnector.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.cptingle.MCAdminConnector.Host;

public class DBConsumer implements Runnable {
	private final BlockingQueue<Query> queue;
	private Connection conn;

	public DBConsumer(BlockingQueue<Query> q, Connect connect) {
		queue = q;
		conn = connect.getConnection();
	}

	public void run() {
		try {
			while (true) {
				consume(queue.take());
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	void consume(Query q) {
		
		/*if (!Host.getManager().isValidServer(q.getCaller())) {
			try {
				PreparedStatement preparedStmt = conn.prepareStatement("INSERT INTO server_DB_queue (server_ID, stored_object) VALUES (?,?)");
				preparedStmt.setString(1, q.getCaller().getID());
				preparedStmt.setObject(2, q);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return;
		}*/
		
		try {
			PreparedStatement preparedStmt = conn.prepareStatement(q.getQuery());
			for (int i = 1; i <= q.getParameters().size(); i++) {
				Object param = q.getParameters().get(i - 1);

				try {
					if (param instanceof String) {
						preparedStmt.setString(i, (String) param);
					} else if (param instanceof Boolean) {
						preparedStmt.setBoolean(i, (Boolean) param);
					} else if (param instanceof Integer) {
						preparedStmt.setInt(i, (Integer) param);
					} else {
						System.out.println("invalid type");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}


			if (!q.getQuery().toLowerCase().contains("select")) {
				preparedStmt.executeUpdate();
			} else {
				preparedStmt.execute();
				ResultSet rs = preparedStmt.getResultSet();

				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();
				String[] columnNames = new String[columnCount];
				// The column count starts from 1
				for (int i = 1; i <= columnCount; i++) {
					String name = rsmd.getColumnName(i);
					columnNames[i - 1] = name;
				}

				ArrayList<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
				while (rs.next()) {
					Map<String, Object> row = new HashMap<String, Object>();
					for (int i = 0; i < columnNames.length; i++) {
						row.put(columnNames[i], rs.getObject(columnNames[i]));
					}
					rows.add(row);
				}

				q.getCaller().processQueryResult(new QueryResult(q.getType(), rows));

				rs.close();
			}

			preparedStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
