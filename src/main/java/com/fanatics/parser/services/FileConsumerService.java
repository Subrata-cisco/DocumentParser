package com.fanatics.parser.services;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.fanatics.parser.ParserConstants;
import com.fanatics.parser.concurrency.ConsumerQueuePayload;
import com.fanatics.parser.contracts.IParser;
import com.fanatics.parser.contracts.IService;
import com.fanatics.parser.logger.FanaticLogger;

public class FileConsumerService implements IService {

	boolean isDestroyRequested = false;
	private ExecutorService service = null;
	private AtomicInteger fileProcessed = new AtomicInteger();
	private BlockingQueue<ConsumerQueuePayload> fileStore = null;
	
	@Override
	public void init() {
		FanaticLogger.log("FileConsumerService.init()", "Starting the service to listen for files to be processed !!");
		BlockingQueue<ConsumerQueuePayload> fileStore = new LinkedBlockingQueue<ConsumerQueuePayload>();
		GlobalServiceProvider.getInstance().addGlobalProperty(ParserConstants.FILE_CONSUMER_QUEUE,fileStore);
		
		int totalThreads = getNoOfThreads();
		service = Executors.newFixedThreadPool(totalThreads);
		new Thread(new QueueConsumer(fileStore)).start();
		
	}

	private int getNoOfThreads() {
		int totalThreads = 5;
		try {
			String noOfthreads = (String)GlobalServiceProvider.getInstance().getProperty(ParserConstants.NUMBER_OF_THREADS);
			totalThreads = Integer.parseInt(noOfthreads);
		} catch (NumberFormatException exception) {
            // nothing to handle			
		}
		FanaticLogger.log("FileConsumerService.getNoOfThreads()", " ::"+totalThreads);
		return totalThreads;
	}

	@Override
	public void destroy() {
		FanaticLogger.log("FileConsumerService.destroy()", "Starting the service to listen for files to be processed !!");
		isDestroyRequested = true;
		if(service != null){
			service.shutdown();
			service = null;
		}
		
		// add some poison , so that it can die gracefully.
		if(fileStore != null){
			fileStore.add(null);
		}
	}
	
	private class QueueConsumer implements Runnable {
		BlockingQueue<ConsumerQueuePayload> queue;
		
		public QueueConsumer(BlockingQueue<ConsumerQueuePayload> queue) {
			this.queue = queue;
		}

		@Override
		public void run() {
			while(!isDestroyRequested){
				try {
					ConsumerQueuePayload payload = queue.take();
					if(payload != null){
						Thread fileProcessingThread = new Thread(new FileProcessor(payload));
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
	
	private class FileProcessor implements Runnable {
		ConsumerQueuePayload payload = null;
				
		FileProcessor(ConsumerQueuePayload payload){
			this.payload = payload;
		}
		
		@Override
		public void run() {
			int type = payload.getFileFormatType();
			IParser parser = MimeTypeFileReaderFactory.getInstance().createFactory(type);
			if(parser != null){
				File file = payload.getFile();
				FanaticLogger.log("FileProcessor.run()", "Started the thread to process file :: "+file.getName()+" for format type ::"+type);
				parser.parseDocument(file);
				fileProcessed.incrementAndGet();
				shutDownWhenAllFileProcessingIsDone();
			}
		}
	}
	
	int tototalFilesToBeProcessed = -1;
	private void shutDownWhenAllFileProcessingIsDone(){
		if(tototalFilesToBeProcessed == -1){
			tototalFilesToBeProcessed = (int) GlobalServiceProvider.getInstance().getProperty(ParserConstants.TOTAL_FILES_TO_PROCESS);
		}
		
		if(tototalFilesToBeProcessed != -1 && tototalFilesToBeProcessed == fileProcessed.get()){
			FanaticLogger.log("FileConsumerService.shutDownWhenAllFileProcessingIsDone()", "FileConsumerService is done with its work , so shutting down !!");
			destroy();
		}
	}
}
