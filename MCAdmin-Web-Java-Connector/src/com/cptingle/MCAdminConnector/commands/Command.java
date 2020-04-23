package com.cptingle.MCAdminConnector.commands;

public interface Command {
	
	public boolean executeCommand(String sender, String label, String[] args);
}
