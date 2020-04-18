package com.cptingle.MCAdminConnector.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.cptingle.MCAdminConnector.MCServer;

public class Query implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 11234565476L;
	
	protected String query;
	protected List<Object> parameters;
	protected MCServer caller;
	protected QueryType type;

	public Query(MCServer caller, String query, List<Object> parameters, QueryType qt) {
		this.query = query;
		this.parameters = parameters;
		if (this.parameters == null) {
			this.parameters = new ArrayList<Object>();
		}
		this.caller = caller;
		this.type = qt;
	}

	public Query(MCServer caller, String query, QueryType qt) {
		this.query = query;
		this.parameters = new ArrayList<Object>();
		this.caller = caller;
		this.type = qt;
	}

	public MCServer getCaller() {
		return caller;
	}

	public String getQuery() {
		return query;
	}

	public List<Object> getParameters() {
		return parameters;
	}

	public QueryType getType() {
		return type;
	}

}
