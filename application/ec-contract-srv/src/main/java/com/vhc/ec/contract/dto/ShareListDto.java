package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@ToString
public class ShareListDto {

    @NotNull
    private Collection<String> email;

    @JsonProperty("contract_id")
    private int contractId;
}
