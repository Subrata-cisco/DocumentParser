package com.fanatics.parser.services;

import com.fanatics.parser.ParserConstants;
import com.fanatics.parser.appender.CSVAppender;
import com.fanatics.parser.contracts.IAppender;
import com.fanatics.parser.contracts.IAppenderFactory;

public class AppenderFactory implements IAppenderFactory {

	private AppenderFactory(){
		
	}
	
	public static AppenderFactory getInstance(){
		return AppenderFactoryHolder.appenderFactory ;
	}
	
	@Override
	public IAppender getAppender(String type) {
		IAppender appender = null;
		switch(type){
		   case ParserConstants.CSV_FORMAT:
			   appender = new CSVAppender();
		   break;
		}
		return appender;
	}

	static class AppenderFactoryHolder {
		static AppenderFactory appenderFactory = new AppenderFactory(); 
	}

}
  