package com.fanatics.parser.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import com.fanatics.parser.ParserConstants;
import com.fanatics.parser.appender.LogEvent;
import com.fanatics.parser.contracts.IAppender;
import com.fanatics.parser.contracts.IParser;
import com.fanatics.parser.logger.FanaticLogger;
import com.fanatics.parser.services.AppenderFactory;
import com.fanatics.parser.services.GlobalServiceProvider;

public class FanaticPDFParser implements IParser {
	@SuppressWarnings("deprecation")
	@Override
	public void parseDocument(File file) {
		FanaticLogger.log("PDFParser.parseDocument()", "Started parsing the file ::"+file.getName());
		String appenderType = (String) GlobalServiceProvider.getInstance().getProperty(ParserConstants.APPENDER);
		IAppender appender = AppenderFactory.getInstance().getAppender(appenderType);
		if(appender != null){
			extractInfo(file,appender);
		}
	}
	
	private void extractInfo(File file,IAppender appender){
		/*PasswordProvider provider = new PasswordProvider() {
			@Override
			public String getPassword(Metadata metadata) {
				return "MAMI4532**";
			}
		};
		ParseContext context = new ParseContext();
		context.set(PasswordProvider.class, provider); */
		
		try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
			
			
			ContentHandler textHandler = new BodyContentHandler(){
			 	@Override
	            public void characters(char[] ch, int start, int length) {
	                String str = new String(ch, start, length);
	                LogEvent event = new LogEvent(str);
		            appender.append(event);  
	            }
	        };
			
	        PDFParser parser = new PDFParser();
	        ParseContext context = new ParseContext();
	        Metadata metadata = new Metadata();
			parser.parse(stream, textHandler, metadata,context);
			
		}catch (Exception e) {
			FanaticLogger.log("FanaticPDFParser.parseDocument()", "Stopeed parsing the document "+file.getName()+" for error ::"+e.getMessage());
		} finally {
			addInCSVQueue(appender.getResult());
		}
	}

	private void addInCSVQueue(List<String> result) {
		BlockingQueue<List<String>> csvStore = (BlockingQueue) GlobalServiceProvider.getInstance().getProperty(ParserConstants.CSV_GENERATOR_QUEUE);
		csvStore.add(result);
	}
	
	
	
	
	
}
