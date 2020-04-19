package com.cptingle.MCAdminConnector.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

	public Logger() {

	}
	
	public void log(LogLevel ll, String message) {
		System.out.println(formatPrefix(ll) + message);
	}
	
	public void info(String message) {
		log(LogLevel.INFO, message);
	}
	
	public void warning(String message) {
		log(LogLevel.WARNING, message);
	}
	
	public void severe(String message) {
		log(LogLevel.SEVERE, message);
	}
	
	private String getThreadName() {
		return Thread.currentThread().getName();
	}

	private String getTimeString() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		return "[" + dtf.format(now) + "]";
	}
	
	private String formatPrefix(LogLevel ll) {
		String val = getTimeString();
		val += " ";
		val += "[" + getThreadName() + "/" + ll.toString() + "]: ";
		return val;
	}

}
