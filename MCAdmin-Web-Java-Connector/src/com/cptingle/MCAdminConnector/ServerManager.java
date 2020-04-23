package com.cptingle.MCAdminConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerManager {

	Map<String, MCServer> serverMap;
	List<MCServer> serverHolding;

	public ServerManager() {
		serverMap = new HashMap<String, MCServer>();
		serverHolding = new ArrayList<MCServer>();
	}
	
	public boolean holdServer(MCServer server) {
		return serverHolding.add(server);
	}
	
	public boolean transferServer(MCServer server) {
		if (isValidServer(server.getToken())) {
			return false;
		} else {
			serverHolding.remove(server);
			serverMap.put(server.getToken(), server);
			return true;
		}
	}
	
	public boolean removeServer(MCServer server) {
		return serverMap.remove(server.getToken()) != null;
	}
	
	public boolean isValidServer(MCServer server) {
		return serverMap.containsValue(server);
	}

	public boolean isValidServer(String token) {
		if (serverMap.containsKey(token))
			return true;
		
		return false;
	}
	
	public MCServer getServer(String id) {
		MCServer s = null;
		for (Map.Entry<String, MCServer> entry : serverMap.entrySet()) {
            if (entry.getValue().getID().equals(id)) {
            	s = entry.getValue();
            }
		}
		return s;
	}
	
	public void shutDown() {
		for (Map.Entry<String, MCServer> entry : serverMap.entrySet()) {
            entry.getValue().close();
		}
	}

}
