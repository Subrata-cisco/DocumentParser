package com.fanatics.parser.contracts;

public interface IAppenderFactory {
	public IAppender getAppender(String type);
}
