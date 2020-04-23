package com.cptingle.MCAdminConnector.commands;

import com.cptingle.MCAdminConnector.Host;
import com.cptingle.MCAdminConnector.MCServer;
import com.cptingle.MCAdminConnector.ServerManager;
import com.cptingle.MCAdminItems.KickRequest;

public class KickCommand implements Command {

	@Override
	public boolean executeCommand(String sender, String label, String[] args) {
		if (args.length < 3) {
			return false;
		}
		
		String serverID = args[0];
		String username = args[1];
		String uuid = args[2];
		String message = "You have been kicked!";
		if (args.length > 3)
			message = allArgs(3, args);
		
		ServerManager sm = Host.getManager();
		MCServer serv = sm.getServer(serverID);
		if (serv == null)
			return false;
		
		KickRequest kr = new KickRequest(username, uuid, message, sender);
		serv.processPlayerKick(kr);
		serv.send(kr);
		return true;
		
	}
	
	public String allArgs(int start , String[] args){
	    String temp = "";
	    for(int i = start ; i < args.length ; i++){
	     temp += args[i] + " "; 
	    }
	   return temp.trim();
	}


}
