package com.vhc.ec.filemgmt.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

/*
*<p>
* Trivial implementation of the {@link MultipartFile} interface to wrap a byte[] decoded
* from a BASE64 encoded String
*</p>
*/
public class BASE64DecodedMultipartFile implements MultipartFile {
	private final byte[] imgContent;
	private final String name;
	private final String originalFilename;
	private final String contentType;

	public BASE64DecodedMultipartFile(byte[] imgContent, String name, String originalFilename, String contentType) {
		this.imgContent = imgContent;
		this.name = name; 
		this.originalFilename = originalFilename; 
		this.contentType = contentType;  
	}

	@Override
	public String getName() { 
		return name;
	}

	@Override
	public String getOriginalFilename() { 
		return originalFilename;
	}

	@Override
	public String getContentType() { 
		return contentType;
	}

	@Override
	public boolean isEmpty() {
		return imgContent == null || imgContent.length == 0;
	}

	@Override
	public long getSize() {
		return imgContent.length;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return imgContent;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(imgContent);
	}

	@Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
		try (FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
			fileOutputStream.write(imgContent);
		}
	}
}
