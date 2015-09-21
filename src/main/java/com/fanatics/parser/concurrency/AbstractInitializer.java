package com.fanatics.parser.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

public abstract class AbstractInitializer implements Callable<Boolean>  { 

	CyclicBarrier barrier = null;
	String serviceName = null;
	
	public AbstractInitializer(String svcName,CyclicBarrier latch){
		this.barrier = latch;
		this.serviceName = svcName;
	}
	
	public Boolean call() throws Exception {
		boolean isUp = false;
		System.out.println("Initiating Service ::"+serviceName+" !!");
		try {
			isInitialized();
			isUp = true;
		} catch (Throwable e) {
			// catch error.
		}finally{
			// we should do this in finally or else it can be a deadlock if calling method gives exception in some way.
			barrier.await();
		}
		return isUp;
	}
	
	public String getServiceName(){
		return this.serviceName;
	}

	public abstract boolean isInitialized();
	
}
