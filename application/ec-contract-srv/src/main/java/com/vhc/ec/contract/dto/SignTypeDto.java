package com.vhc.ec.contract.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class SignTypeDto implements Serializable{
	private int id;
    private String name;
    @JsonProperty("is_otp")
    private Boolean isOtp;
}
