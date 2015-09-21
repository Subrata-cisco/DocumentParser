package com.fanatics.parser.concurrency;

import java.io.File;

public class ConsumerQueuePayload {
	
	private File file;
	private int fileFormatType;
	
	public ConsumerQueuePayload(File file, int fileFormatType) {
		this.file = file;
		this.fileFormatType = fileFormatType;
	}

	public File getFile() {
		return file;
	}

	public int getFileFormatType() {
		return fileFormatType;
	}
}
