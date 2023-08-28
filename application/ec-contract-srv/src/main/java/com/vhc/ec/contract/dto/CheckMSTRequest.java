package com.vhc.ec.contract.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class CheckMSTRequest {
	@NotBlank
	private String mst;
	
	@NotBlank
	private String certB64;
}
