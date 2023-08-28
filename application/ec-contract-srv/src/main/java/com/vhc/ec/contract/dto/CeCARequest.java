package com.vhc.ec.contract.dto;

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
public class CeCARequest {
	private String hexEncodeFile;
	private String senderId;
	private String messageId;
	private String fileName;
	private int orgId;
}
