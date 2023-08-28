package com.vhc.ec.notification.dto; 
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class ContractOriginalLinkDto implements Serializable{
	
	private int id;
	
    private String code;
	
    @JsonProperty("original_link") 
    private String originalLink; 
}
