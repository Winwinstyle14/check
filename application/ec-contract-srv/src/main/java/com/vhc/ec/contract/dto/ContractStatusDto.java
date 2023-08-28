package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@ToString
public class ContractStatusDto {

    @JsonProperty("processed")
    private final long processed;

    @JsonProperty("processing")
    private final long processing;

    @JsonProperty("waiting")
    private final long waiting;

    @JsonProperty("prepare_expires")
    private final long prepareExpires;

}
