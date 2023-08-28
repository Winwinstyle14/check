package com.vhc.ec.filemgmt.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
public class UploadCeCARequest {
	@NotBlank
	private String hexEncodeFile;
	
	@NotBlank
	private String fileName;
	
	@NotNull
	private Integer orgId;
}
