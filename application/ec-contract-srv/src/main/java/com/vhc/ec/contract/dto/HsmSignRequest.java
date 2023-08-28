package com.vhc.ec.contract.dto;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HsmSignRequest {
	@NotBlank
	private String username;
	 
	@NotBlank
	private String password;
	
	@NotBlank
	@JsonProperty("ma_dvcs")
	private String taxCode;
	
	@NotBlank
	private String password2; 
	
	@NotBlank
	@JsonProperty("image_base64")
	private String imageBase64;
}
