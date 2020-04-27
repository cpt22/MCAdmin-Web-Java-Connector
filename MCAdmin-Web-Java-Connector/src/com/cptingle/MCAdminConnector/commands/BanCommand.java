package com.cptingle.MCAdminConnector.commands;

import com.cptingle.MCAdminConnector.Host;
import com.cptingle.MCAdminConnector.MCServer;
import com.cptingle.MCAdminConnector.ServerManager;
import com.cptingle.MCAdminItems.BanRequest;

public class BanCommand implements Command {

	@Override
	public boolean executeCommand(String sender, String label, String[] args) {
		if (args.length < 3) {
			Host.getServer().getLogger().info("Incorrect number of arguments");
			return false;
		}

		String serverID = args[0];
		String username = args[1];
		String uuid = args[2];
		String message = "You have been banned!";
		if (args.length > 3)
			message = allArgs(3, args);

		ServerManager sm = Host.getManager();
		MCServer serv = sm.getServer(serverID);
		if (serv == null)
			return false;

		BanRequest br = new BanRequest(username, uuid, message, sender, true);
		serv.processPlayerBan(br);
		serv.send(br);

		return true;

	}

	public String allArgs(int start, String[] args) {
		String temp = "";
		for (int i = start; i < args.length; i++) {
			temp += args[i] + " ";
		}
		return temp.trim();
	}

}
