package com.cptingle.MCAdminConnector;

import java.util.Scanner;

public class Server extends Thread {
	Scanner consoleInput;

	public Server() {
		consoleInput = new Scanner(System.in);
	}

	public void run() {
		while (true) {
			String ging = consoleInput.nextLine().toLowerCase();

			if (ging.equals("stop") || ging.equals("exit")) {
				System.out.print("Closing Server");

				try {
					System.out.print(".");
					sleep(100);
					System.out.print(".");
					sleep(100);
					System.out.println(".");
					sleep(500);
				} catch (Exception e) {}
				System.exit(0);
			} else if (ging.equals("mem") || ging.equals("memory")) {
				System.out.println("Total Memory Allocated: " + Runtime.getRuntime().totalMemory() / 1000000 + "M");
				System.out.println("Free Memory Remaining: " + Runtime.getRuntime().freeMemory() / 1000000 + "M");
				System.out.println("Max Memory Allocated: " + Runtime.getRuntime().maxMemory() / 1000000 + "M");
			}
		}
	}
}
