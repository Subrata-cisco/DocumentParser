package com.fanatics.parser.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;

public class MimeTypeFinder implements IMimeTypeFinder {

	@Override
	public String detectMimeType(File file) {
		String mimeType = null;
		Metadata metadata = new Metadata();
		MimeTypes mimeTypes = TikaConfig.getDefaultConfig().getMimeRepository();
		try (BufferedInputStream stream = new BufferedInputStream(
				new FileInputStream(file))) {
			MediaType mediaType = mimeTypes.detect(stream, metadata);
			if(mediaType != null){
				mimeType = mediaType.getSubtype();
			}
		} catch (Exception e) {
			System.out.println("MimeTypeFinder.detectMimeType() Error ::" + e.getMessage());
		}
		return mimeType;
	}
}
