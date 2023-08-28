package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@ToString
public class SimPkiV3Dto {

    @NotBlank
    private String mobile;

    @NotBlank
    @JsonProperty("network_code")
    private String networkCode;

    private String prompt;

    private String reason;

    @NotBlank
    @JsonProperty("image_base64")
    private String imageBase64;

}
