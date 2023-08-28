package com.vhc.ec.filemgmt.controller;

import java.io.FileInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhc.ec.api.versioning.ApiVersion;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class TmpController {
	@GetMapping(value = "/tmp/{fileName}")
    public ResponseEntity<ByteArrayResource> getFile(
    		@PathVariable("fileName") String fileName, HttpServletRequest request) {
    	MediaType mediaType = null;
		byte[] bytes = null;
		final var temFilePath = String.format("./tmp/file/%s", fileName); 
		
		//log.info("temFilePath = "+temFilePath);
		//System.out.println("temFilePath = "+temFilePath);
		
		try {
			try (var input = new FileInputStream(temFilePath)) {
				
				//Lấy mimeType từ tên file
				//Path path = new File(temFilePath).toPath();
			    //String mimeType = Files.probeContentType(path);
			    
			    // Try to determine file's content type
		        String mimeType = request.getServletContext().getMimeType(temFilePath);
		        
		        //System.out.println("mimeType = "+mimeType);

		        // Fallback to the default content type if type could not be determined
		        if(mimeType == null) {
		        	mimeType = "application/octet-stream";
		        } 
			    
			    //Convert mimeType to MediaType
			    mediaType = MediaType.parseMediaType(mimeType);  
			    
			    //Convert InputStream to byte[]
				bytes = input.readAllBytes();
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		 
		return ResponseEntity
                .ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)//link download
                .contentLength(bytes.length) 
                .body(new ByteArrayResource(bytes));
    }
}
