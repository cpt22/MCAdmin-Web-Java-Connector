package com.cptingle.MCAdminConnector;

import java.util.Scanner;

import com.cptingle.MCAdminConnector.logging.Logger;

public class Server extends Thread {
	private Scanner consoleInput;
	private Logger logger;

	public Server() {
		consoleInput = new Scanner(System.in);
		logger = new Logger();
		this.setName("Console thread");
	}

	public void run() {
		while (true) {
			String input = consoleInput.nextLine().toLowerCase();

			switch (input) {
			case "exit":
			case "stop":

				break;

			}

			if (input.equals("stop") || input.equals("exit")) {
				logger.info("Closing Server");

				try {
					/*System.out.print(".");
					sleep(100);
					System.out.print(".");
					sleep(100);
					System.out.println(".");*/
					sleep(500);
				} catch (Exception e) {
				}
				System.exit(0);
			} else if (input.equals("mem") || input.equals("memory")) {
				System.out.println("Total Memory Allocated: " + Runtime.getRuntime().totalMemory() / 1000000 + "M");
				System.out.println("Free Memory Remaining: " + Runtime.getRuntime().freeMemory() / 1000000 + "M");
				System.out.println("Max Memory Allocated: " + Runtime.getRuntime().maxMemory() / 1000000 + "M");
			}
		}
	}

	public Logger getLogger() {
		return logger;
	}

}
