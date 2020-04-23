package com.cptingle.MCAdminConnector;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.cptingle.MCAdminConnector.commands.BanCommand;
import com.cptingle.MCAdminConnector.commands.Command;
import com.cptingle.MCAdminConnector.commands.KickCommand;
import com.cptingle.MCAdminConnector.commands.UnbanCommand;
import com.cptingle.MCAdminConnector.logging.Logger;

public class Server extends Thread {
	private Scanner consoleInput;
	private Logger logger;
	private Map<String, Command> commandExecutors;

	public Server() {
		consoleInput = new Scanner(System.in);
		logger = new Logger();
		this.setName("Console thread");
		
		commandExecutors = new HashMap<String, Command>();
		registerListeners();
	}
	
	private void registerListeners() {
		addCommandExecutor("kick", new KickCommand());
		addCommandExecutor("ban", new BanCommand());
		addCommandExecutor("unban", new UnbanCommand());
	}
	
	public void addCommandExecutor(String label, Command c) {
		this.commandExecutors.put(label.toLowerCase(), c);
	}

	public void run() {
		while (true) {
			String input = consoleInput.nextLine().toLowerCase();

			if (input.equals("stop") || input.equals("exit")) {
				logger.info("Closing Server");

				try {
					sleep(500);
				} catch (Exception e) {
				}
				System.exit(0);
			} else if (input.equals("mem") || input.equals("memory")) {
				System.out.println("Total Memory Allocated: " + Runtime.getRuntime().totalMemory() / 1000000 + "M");
				System.out.println("Free Memory Remaining: " + Runtime.getRuntime().freeMemory() / 1000000 + "M");
				System.out.println("Max Memory Allocated: " + Runtime.getRuntime().maxMemory() / 1000000 + "M");
			} else {
				String sender = "sender";
				String[] splitCmd = input.split(" ");
				String label = splitCmd[0];
				String[] args = new String[splitCmd.length - 1];
				for (int i = 1; i < splitCmd.length; i++) {
					args[i-1] = splitCmd[i];
				}
				Command c = commandExecutors.get(label);
				if (c != null) {
					c.executeCommand(sender, label, args);
				} else {
					getLogger().warning("Command " + label + " has no executor");
				}
			}
		}
	}

	public Logger getLogger() {
		return logger;
	}

}
