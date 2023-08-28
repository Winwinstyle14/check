package com.vhc.ec.contract.dto;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SmsLogRequest {
	@NotBlank
	private String isdn;
	
	@NotBlank
	private String mtcontent;

	private int orgId;
	
	private String brandName;
    
    private String smsUser;
    
    private String smsPass;
    
    @NotBlank
    private String smsSendMethor;

	private Integer contractId;
}
