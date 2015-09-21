package com.fanatics.parser;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import com.fanatics.parser.concurrency.ConsumerQueuePayload;
import com.fanatics.parser.contracts.IFolderReader;
import com.fanatics.parser.exception.ParseException;
import com.fanatics.parser.logger.FanaticLogger;
import com.fanatics.parser.parser.IMimeTypeFinder;
import com.fanatics.parser.parser.MimeTypeFinder;
import com.fanatics.parser.services.GlobalServiceProvider;

public class FolderReader implements IFolderReader {

	private HashMap<String,Integer> formatReaders = null;
	private BlockingQueue<ConsumerQueuePayload> fileToBeProcessed = null;
	private int totalFilesToBeProcessed = 0;
	
	
	@SuppressWarnings("unchecked")
	public FolderReader() {
		formatReaders = (HashMap<String,Integer>) GlobalServiceProvider
				.getInstance().getProperty(ParserConstants.VALID_FILE_FORMAT);
		fileToBeProcessed = (BlockingQueue<ConsumerQueuePayload>)GlobalServiceProvider
				.getInstance().getProperty(ParserConstants.FILE_CONSUMER_QUEUE);
	}

	@Override
	public void processFolder(String path) {
		File file = new File(path);
		boolean isExist = file.exists();
		
		if (isExist && file.isDirectory()) {
			File[] directoryListing = file.listFiles(); 
			if (directoryListing != null) {
				for (File childFile : directoryListing) {
					processFile(childFile);
				}
			}
		}else if(isExist && file.isFile()){
			processFile(file);
		}
		
		if(!isExist){
			throw new ParseException(ParseException.FILE_PATH_NOT_VALID,path);
		}else{
			GlobalServiceProvider.getInstance().addGlobalProperty(ParserConstants.TOTAL_FILES_TO_PROCESS, totalFilesToBeProcessed);
		}
	}
	
	private void processFile(File file){
		FanaticLogger.log("FolderReader.processFile()", "Process file ::"+file.getName());
		IMimeTypeFinder iMimeTypeFinder = new MimeTypeFinder();
		String mimeType = iMimeTypeFinder.detectMimeType(file);
		
		if(formatReaders.containsKey(mimeType)){
			FanaticLogger.log("FolderReader.processFile()", " File added "+file.getName()+" added for processing !!");
			ConsumerQueuePayload payload = new ConsumerQueuePayload(file,formatReaders.get(mimeType)); 
			totalFilesToBeProcessed ++;
			fileToBeProcessed.add(payload);
		}else{
			FanaticLogger.log("FolderReader.processFile()", " File format for "+file.getName()+" not supported , so ignored !!");
		}
	}
} 
