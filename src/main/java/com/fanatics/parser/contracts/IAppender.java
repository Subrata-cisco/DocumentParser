package com.fanatics.parser.contracts;

import java.util.List;

import com.fanatics.parser.appender.LogEvent;

public interface IAppender {
   public boolean append(LogEvent event);
   public List<String> getResult();
}
