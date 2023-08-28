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
public class StatisticDto {
    @JsonProperty("total_draff")
    private final long totalDraff;

    @JsonProperty("total_created")
    private final long totalCreated;

    @JsonProperty("total_cancel")
    private final long totalCancel;

    @JsonProperty("total_reject")
    private final long totalReject;

    @JsonProperty("total_signed")
    private final long totalSigned;

    @JsonProperty("total_process")
    private final long totalProcess;

    @JsonProperty("total_expires")
    private final long totalExpires;
}
