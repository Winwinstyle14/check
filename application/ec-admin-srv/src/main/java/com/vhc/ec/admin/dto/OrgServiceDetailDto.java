package com.vhc.ec.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vhc.ec.admin.constant.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OrgServiceDetailDto {
    private String code;

    private String name;

    private long totalBeforeVAT;

    private long totalAfterVAT;

    private CalculatorMethod calculatorMethod;

    private Integer duration;

    private Integer numberOfContracts;

    private ServiceType serviceType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    private PaymentType paymentType;

    private PaymentStatus paymentStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private UsageStatus usageStatus;

    private UsageStatus getUsageStatus() {
        if (calculatorMethod == CalculatorMethod.BY_TIME) {
            LocalDate now = LocalDate.now();
            if (now.isBefore(startDate)) {
                return UsageStatus.NOT_USED;
            } else if (now.isAfter(endDate)) {
                return UsageStatus.FINISHED;
            }

            return UsageStatus.USING;
        }
        return usageStatus;
    }
}
