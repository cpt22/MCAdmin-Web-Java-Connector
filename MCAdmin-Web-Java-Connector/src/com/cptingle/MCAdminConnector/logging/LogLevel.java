package com.cptingle.MCAdminConnector.logging;

public enum LogLevel {
	INFO("INFO"),
	WARNING("WARN"),
	SEVERE("ERROR");
	
	private final String value;
	
	private LogLevel(String val) {
		this.value = val;
	}
	
	public String toString() {
		return value;
	}

}
