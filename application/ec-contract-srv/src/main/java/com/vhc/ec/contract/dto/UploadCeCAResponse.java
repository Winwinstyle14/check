package com.vhc.ec.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class UploadCeCAResponse {
	private boolean success;

    private String message;
 
    private String filePath;
 
    private String filename;

    private String bucket;
}
