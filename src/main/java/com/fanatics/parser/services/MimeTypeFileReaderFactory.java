package com.fanatics.parser.services;

import com.fanatics.parser.ParserConstants;
import com.fanatics.parser.contracts.IParser;
import com.fanatics.parser.contracts.IParserFactory;
import com.fanatics.parser.parser.FanaticPDFParser;

public class MimeTypeFileReaderFactory implements IParserFactory {

	private MimeTypeFileReaderFactory(){
		
	}
	
	public static MimeTypeFileReaderFactory getInstance(){
		return MimeTypeFileReaderFactoryHolder.mimeTypeFileReaderFactory ;
	}
	
	@Override
	public IParser createFactory(int type) {
		IParser reader = null;
		switch(type){
		   case ParserConstants.FILE_FORMAT_PDF:
			   reader = new FanaticPDFParser();
		   break;
		}
		return reader;
	}

	static class MimeTypeFileReaderFactoryHolder {
		static MimeTypeFileReaderFactory mimeTypeFileReaderFactory = new MimeTypeFileReaderFactory();
	}
}
