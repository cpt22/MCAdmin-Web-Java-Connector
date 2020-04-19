package com.cptingle.MCAdminConnector.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StoredQuery implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 113245L;
	
	public String query;
	public List<Object> parameters;
	public String serverID;
	public QueryType type;

	public StoredQuery(String serverID, String query, List<Object> parameters, QueryType qt) {
		this.query = query;
		this.parameters = parameters;
		if (this.parameters == null) {
			this.parameters = new ArrayList<Object>();
		}
		this.serverID = serverID;
		this.type = qt;
	}
}
