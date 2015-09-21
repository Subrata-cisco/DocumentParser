package com.fanatics.parser.concurrency;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.CyclicBarrier;

import com.fanatics.parser.ParserConstants;
import com.fanatics.parser.exception.ParseException;
import com.fanatics.parser.services.GlobalServiceProvider;

public class PropertyInitializer extends AbstractInitializer {

	public PropertyInitializer(String svcName, CyclicBarrier latch) { 
		super(svcName, latch);
	}

	@Override
	public boolean isInitialized() {
		boolean initialized = false;
		
		// load the stream
		InputStream propertyFile = getClass().getClassLoader().getResourceAsStream(ParserConstants.PROPERTY_FILE_NAME);
		if(propertyFile == null){
			throw new ParseException(ParseException.PROPERTY_FILE_NOT_FOUND, ParserConstants.PROPERTY_FILE_NAME +" not found !!");
		}
		
		Properties prop = new Properties();
		try {
			prop.load(propertyFile);
		} catch (IOException exception) {
			// not found
		}
		
		initialized = loadPropertyToGlobalProvider(prop);
		return initialized;
	}

	private boolean loadPropertyToGlobalProvider(Properties prop) {
		boolean initialized = false;
		
		loadTotalThreadsToBeUsed(prop);
		
		loadFileFormatAndParser(prop);
		
		loadRepository(prop);
		
		loaddAppender(prop);
		
		initialized = true;
		
		return initialized;
	}

	private void loaddAppender(Properties prop) {
		String appender = prop.getProperty("APPENDER_TYPE");
		if(appender == null){
			throw new ParseException(ParseException.APPENDER_NOT_FOUND,appender+" destination output format not found (e.g csv,database)!!");
		}else{
			GlobalServiceProvider.getInstance().addGlobalProperty(ParserConstants.APPENDER, appender);
		}
	}

	private void loadTotalThreadsToBeUsed(Properties prop) {
		GlobalServiceProvider.getInstance().addGlobalProperty(ParserConstants.NUMBER_OF_THREADS, prop.getProperty("NO_OF_THREADS_FOR_FILE_PROCESSING"));
	}

	private void loadRepository(Properties prop) {
		String repoPath = prop.getProperty("REPOSITORY");
		if(repoPath == null || (repoPath != null && !new File(repoPath).isDirectory())){
			throw new ParseException(ParseException.FOLDER_PATH_NOT_VALID,repoPath+" is not a valid folder (which contains all files to be analysed)!!");
		}else{
			GlobalServiceProvider.getInstance().addGlobalProperty(ParserConstants.REPO_PATH, repoPath);
			
		}
	}

	private void loadFileFormatAndParser(Properties prop) {
		String fileTypeSupported = prop.getProperty("FILE_TYPE_SUPPORTED");
		if(fileTypeSupported == null){
			throw new ParseException(ParseException.VALID_FILE_FORMAT_NOT_FOUND,"VALID_FILE_FORMAT_NOT_FOUND !!");
		}else{
			HashMap<String,Integer> formatReaders = new HashMap<>();
			String[] allFormats = fileTypeSupported.split(",");
			for(String format : allFormats){
				switch(format){
				   case ParserConstants.PDF_FORMAT:
					   formatReaders.put(format,ParserConstants.FILE_FORMAT_PDF);
					   break;
				}
			}
			GlobalServiceProvider.getInstance().addGlobalProperty(ParserConstants.VALID_FILE_FORMAT, formatReaders);
		}
	}
}
