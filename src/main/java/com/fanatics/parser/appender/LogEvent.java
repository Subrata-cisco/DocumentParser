package com.fanatics.parser.appender;

public class LogEvent {
	
    int type ; 
    String data;
    
	public LogEvent(String data) {
		this.data = data;
	}

	public LogEvent(int type, String data) {
		this.type = type;
		this.data = data;
	}

	public int getType() {
		return type;
	}

	public String getData() {
		return data;
	}
}
