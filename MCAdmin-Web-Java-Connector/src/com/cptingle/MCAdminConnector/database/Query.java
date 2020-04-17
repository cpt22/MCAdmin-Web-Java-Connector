package com.cptingle.MCAdminConnector.database;

import java.util.ArrayList;
import java.util.List;

import com.cptingle.MCAdminConnector.MCServer;

public class Query {
	
	private String query;
	private List<Object> parameters;
	private MCServer caller;
	private QueryType type;
	
	public Query(MCServer caller, String query, List<Object> parameters, QueryType qt) {
		this.query = query;
		this.parameters = parameters;
		if (this.parameters == null) {
			this.parameters = new ArrayList<Object>();
		}
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
