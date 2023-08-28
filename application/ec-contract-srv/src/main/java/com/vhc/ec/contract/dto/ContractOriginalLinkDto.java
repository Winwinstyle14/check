package com.vhc.ec.contract.dto;

import static com.vhc.ec.contract.definition.DataFormat.TIMESTAMP_FORMAT;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class ContractOriginalLinkDto implements Serializable{
	
	private int id;
	
    private String code;
	
    @JsonProperty("original_link") 
    private String originalLink;
	
	@JsonProperty("exprire_time")
    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    private Date exprireTime;
}
