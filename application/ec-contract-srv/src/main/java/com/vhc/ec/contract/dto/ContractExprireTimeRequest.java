package com.vhc.ec.contract.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
 
@Data
@NoArgsConstructor
@ToString
public class ContractExprireTimeRequest { 
    private String contractName;
	 
    private String recipientName;
 
    private String recipientEmail; 
    
    private String loginType;
    
    private String contractUrl;
    
    private String contractExpireTime;
}
