package com.fanatics.parser.exception;

public class ParseException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private int id = -1;
	
	public static final int FILE_PATH_NOT_VALID = 1;
	public static final int FOLDER_PATH_NOT_VALID = 2;
	public static final int PROPERTY_FILE_NOT_FOUND = 3;
	public static final int VALID_FILE_FORMAT_NOT_FOUND = 4;
	public static final int APPENDER_NOT_FOUND = 5;
	
	public ParseException(int id , String message){
		super(message);
		this.id = id;
	}

	@Override
	public String toString() {
		return "ParseException [id=" + id + ", getMessage()=" + getMessage()
				+ "]";
	}
	
	public int getID(){
		return id;
	}
	
}
