package com.vhc.ec.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticDto {
    private long totalDraff;

    private long totalCreated;

    private long totalCancel;

    private long totalReject;

    private long totalSigned;

    private long totalProcess;

    private long totalExpires;
}
