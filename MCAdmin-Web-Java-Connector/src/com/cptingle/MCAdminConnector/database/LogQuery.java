package com.cptingle.MCAdminConnector.database;

import java.util.ArrayList;

import com.cptingle.MCAdminConnector.MCServer;

public class LogQuery extends Query {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1523704360058932327L;

	public LogQuery(MCServer caller, String executor, String action, String details) {
		super(caller, "INSERT INTO action_log_server (server_ID, executor, action, details) VALUES (?,?,?,?)", QueryType.LOG);
		this.parameters = new ArrayList<Object>();
		this.parameters.add(caller.getID());
		this.parameters.add(executor);
		this.parameters.add(action);
		this.parameters.add(details);
	}

}
