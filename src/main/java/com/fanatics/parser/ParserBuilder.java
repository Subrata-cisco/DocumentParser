package com.fanatics.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.fanatics.parser.concurrency.AbstractInitializer;
import com.fanatics.parser.concurrency.PropertyInitializer;
import com.fanatics.parser.concurrency.ServiceInitializer;
import com.fanatics.parser.contracts.IService;
import com.fanatics.parser.logger.FanaticLogger;
import com.fanatics.parser.services.GlobalServiceProvider;

public class ParserBuilder {
	
	private static final int TOTAL_READER_INITIALIZER_SERVICE = 2;
	private boolean initialize = false;
	
	public ParserBuilder(){
		
	}
	
	//////////////////////////////////////////////PUBLIC API START//////////////////////////////////////
	
	public void build() {
		initiateSystem();
	}

	private void initiateProcessing() {
		if(initialize){
			//String path = (String)GlobalServiceProvider.getInstance().getProperty(ParserConstants.REPO_PATH);
			String workingDir = System.getProperty("user.dir");
			String path = File.separator+workingDir+File.separator+"document-repository";
		
			FanaticLogger.log("ParserBuilder.initiateProcessing()", "looking for repository folder at ::"+path);
			new FolderReader().processFolder(path);
		}
	}
	
	/**
	 * Will not support cancellation of task once started, as it is small execution as of now.
	 */
	private void initiateSystem(){
		CyclicBarrier waitForInitializers = new CyclicBarrier(TOTAL_READER_INITIALIZER_SERVICE , new IServiceRunner());
		ExecutorService service = Executors.newFixedThreadPool(TOTAL_READER_INITIALIZER_SERVICE); 

		List<AbstractInitializer> list = new ArrayList<>();
		list.add(new PropertyInitializer(ParserConstants.PROPERTY_SERVICE,waitForInitializers));
		list.add(new ServiceInitializer(ParserConstants.SERVICE_MANAGER,waitForInitializers)); 
		
		ArrayList<Future<Boolean>> futureList = new ArrayList<Future<Boolean>>();
		
		for(AbstractInitializer svc: list){
			futureList.add(service.submit(svc));
		}
		
		service.shutdown();
		
		//waitForSystemToInitialize(waitForInitializers);
	}

	private void waitForSystemToInitialize(CyclicBarrier waitForInitializers) {
		while(waitForInitializers.getNumberWaiting() != 0){
			FanaticLogger.log("ParserBuilder.initiateSystem()", "Trying to Initiate the system !!!");
			try {
				TimeUnit.SECONDS.sleep(100);
			} catch (InterruptedException exception) {
				// nothing to handle..
			}
		}
	}
	
	private class IServiceRunner implements Runnable {
		@Override
		public void run() {
			List<IService> serviceList = (List<IService>)GlobalServiceProvider.getInstance().getProperty(ParserConstants.SERVICE_LIST);
			if(serviceList != null){
				for(IService service : serviceList){
					service.init();
				}
				initialize = true;
			}
			
			if(initialize){
				System.out.println(" All services initalized properly !! starting the analysys !!");
				try {
					initiateProcessing();
				} catch (Exception exception) {
					FanaticLogger.log("IServiceRunner.run()", "initiateProcessing() failed with error ::"+exception.getMessage(),FanaticLogger.ERROR);
				}
			}else{
				FanaticLogger.log("IServiceRunner.run()", "Failed to Initiate the System !!");
			}
		}
	}
	
}	
