package com.vhc.ec.contract.dto;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

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
public class CeCAResponse {
	private String hexEncodeFile;
	private String status;
	private String message;
	
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private Date sendDate;
	
	private String senderId;
	private String transactionId;
	private String messageId;
	private String referenceMessageId;
}
