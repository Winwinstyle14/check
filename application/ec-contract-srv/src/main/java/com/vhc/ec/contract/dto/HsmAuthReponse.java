package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HsmAuthReponse {
	private String token;
	
	@JsonProperty("ma_dvcs")
	private String taxCode;
	
	@JsonProperty("wb_user_id")
	private String wbUserId;
	
	private String error;
}
