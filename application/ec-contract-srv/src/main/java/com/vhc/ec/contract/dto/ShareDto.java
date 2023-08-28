package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
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
@Builder
public class ShareDto {
    private Integer id;

    @NotBlank
    private String email;

    private String token;

    private int status;

    @JsonProperty("contract_id")
    private int contractId;
}
