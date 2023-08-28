package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Data
@ToString
public class SimPkiV2Dto {
    @NotBlank
    private String mobile;

    @NotBlank
    @JsonProperty("network_code")
    private String networkCode;

    private String prompt;

    private String reason;
}
