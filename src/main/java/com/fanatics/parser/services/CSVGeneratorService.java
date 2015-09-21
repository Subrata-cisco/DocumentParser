package com.fanatics.parser.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.fanatics.parser.ParserConstants;
import com.fanatics.parser.contracts.IService;
import com.fanatics.parser.logger.FanaticLogger;
import com.opencsv.CSVWriter;

public class CSVGeneratorService implements IService {

	private boolean isDestroyRequested = false;
	private ExecutorService service = null;
	private String generatedCSVPath = null;
	private AtomicInteger fileProcessed = new AtomicInteger();
	private BlockingQueue<List<String>> csvStore = null;
	
	@Override
	public void init() {
		FanaticLogger.log("CSVGeneratorService.init()", "Starting the service to generate the CSV file for all the processed records !!");
		csvStore = new LinkedBlockingQueue<>();
		GlobalServiceProvider.getInstance().addGlobalProperty(ParserConstants.CSV_GENERATOR_QUEUE,csvStore);
		
		String workingDir = System.getProperty("user.dir");
		generatedCSVPath = File.separator+workingDir+File.separator+"StatementOutput.csv";
		FanaticLogger.log("CSVGeneratorService.init()", "CSV file will be generated as ::"+generatedCSVPath);
		
		int totalThreads = getNoOfThreads();
		service = Executors.newFixedThreadPool(totalThreads);
		new Thread(new CSVQueueConsumer(csvStore)).start();
	}

	@Override
	public void destroy() {
		isDestroyRequested = true;
		if(service != null){
			service.shutdown();
			service = null;
		}
		
		// add some poison , so that it can die gracefully.
		if(csvStore != null){
			csvStore.add(null);
		}
	}
	
	private int getNoOfThreads() {
		int totalThreads = 5;
		try {
			String noOfthreads = (String)GlobalServiceProvider.getInstance().getProperty(ParserConstants.NUMBER_OF_THREADS);
			totalThreads = Integer.parseInt(noOfthreads);
		} catch (NumberFormatException exception) {
            // nothing to handle			
		}
		FanaticLogger.log("CSVGeneratorService.getNoOfThreads()", " ::"+totalThreads);
		return totalThreads;
	}
	
	private class CSVQueueConsumer implements Runnable {
		BlockingQueue<List<String>> queue;
		
		public CSVQueueConsumer(BlockingQueue<List<String>> queue) {
			this.queue = queue;
		}

		@Override
		public void run() {
			while(!isDestroyRequested){
				try {
					List<String> list = queue.take();
					if(list != null){
						Thread fileProcessingThread = new Thread(new CSVGenerator(list));
						service.submit(fileProcessingThread);
					}else{
						isDestroyRequested = true;
					}
					
				} catch (InterruptedException e) {
					isDestroyRequested = true;
				}
			}
		}
		
	}
	
	private class CSVGenerator implements Runnable {
		List<String> payload = null;
				
		CSVGenerator(List<String> payload){
			this.payload = payload;
		}
		
		@Override
		public void run() {
			createCSV(payload);
		}
	}
	
	private void createCSV(List<String> list){
		for(String line : list){
			System.out.println(line);
		    CSVWriter writer = null;
			try {
				writer = new CSVWriter(new FileWriter(generatedCSVPath, true));
				String [] record = line.split(",");
				writer.writeNext(record);
			} catch (IOException exception) {
				FanaticLogger.log("CSVGeneratorService.createCSV()", "error happened while generatinf CSV ::"+exception.getMessage());
			}finally{
				 try {
					writer.close();
				} catch (IOException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				}
			}
		}
		fileProcessed.incrementAndGet();
		shutDownWhenAllFileProcessingIsDone();
	}
	
	int tototalFilesToBeProcessed = -1;
	private void shutDownWhenAllFileProcessingIsDone(){
		if(tototalFilesToBeProcessed == -1){
			tototalFilesToBeProcessed = (int) GlobalServiceProvider.getInstance().getProperty(ParserConstants.TOTAL_FILES_TO_PROCESS);
		}
		
		if(tototalFilesToBeProcessed != -1 && tototalFilesToBeProcessed == fileProcessed.get()){
			FanaticLogger.log("CSVGeneratorService.shutDownWhenAllFileProcessingIsDone()", "CSVGeneratorService is done with its work , Report generation Finished !!");
			destroy();
		}
	}

}
