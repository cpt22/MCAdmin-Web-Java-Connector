package com.cptingle.MCAdminConnector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServerManager {

	Set<MCServer> validServers;
	List<MCServer> serverHolding;

	public ServerManager() {
		validServers = new HashSet<MCServer>();
		serverHolding = new ArrayList<MCServer>();
	}
	
	public boolean holdServer(MCServer server) {
		return serverHolding.add(server);
	}
	
	public boolean transferServer(MCServer server) {
		if (isValidServer(server)) {
			return false;
		} else {
			serverHolding.remove(server);
			validServers.add(server);
			return true;
		}
	}
	
	public boolean removeServer(MCServer server) {
		return validServers.remove(server);
	}
	
	public boolean isValidServer(MCServer server) {
		return validServers.contains(server);
	}
	
	public boolean isValidServer(String serverID) {
		return validServers.contains(serverID);
	}
	
	public MCServer getServer(String id) {
		for (MCServer server : validServers) {
			if (server.getID().equals(id)) {
				return server;
			}
		}
		return null;
	}
	
	public void shutDown() {
		for (MCServer server : validServers) {
			server.close();
		}
	}

}
