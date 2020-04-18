package com.cptingle.MCAdminConnector.database;

import java.util.ArrayList;
import java.util.Map;

public class QueryResult {
	public ArrayList<Map<String, Object>> result;
	public QueryType type;
	
	public QueryResult(QueryType qt, ArrayList<Map<String, Object>> rs) {
		this.result = rs;
		this.type = qt;
	}
}
