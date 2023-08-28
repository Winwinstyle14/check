package com.vhc.ec.notification.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class ContractExprireTimeRequest {
	@NotBlank(message = "contractName is mandatory")
    private String contractName;
	
	@NotBlank(message = "RecipientName is mandatory")
    private String recipientName;

    @NotBlank(message = "RecipientEmail is mandatory")
    private String recipientEmail; 
    
    private String loginType;
    
    @NotBlank(message = "contractUrl is mandatory")
    private String contractUrl;
    
    private String contractExpireTime;
}
