package com.fanatics.parser.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import com.fanatics.parser.ParserConstants;
import com.fanatics.parser.contracts.IService;
import com.fanatics.parser.services.CSVGeneratorService;
import com.fanatics.parser.services.FileConsumerService;
import com.fanatics.parser.services.GlobalServiceProvider;

public class ServiceInitializer extends AbstractInitializer {

	public ServiceInitializer(String svcName, CyclicBarrier latch) {
		super(svcName, latch);
	}

	@Override
	public boolean isInitialized() {
		List<IService> serviceList = new ArrayList<>();
		serviceList.add(new FileConsumerService());
		serviceList.add(new CSVGeneratorService());
		GlobalServiceProvider.getInstance().addGlobalProperty(ParserConstants.SERVICE_LIST, serviceList);
		return true;
	}

}
	