package com.vhc.ec.contract.dto;

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
public class HsmAuthRequest {
	private String username;
	 
	private String password;
	
	@JsonProperty("ma_dvcs")
	private String taxCode;
}
