package com.vhc.ec.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vhc.ec.admin.constant.CalculatorMethod;
import com.vhc.ec.admin.constant.UsageStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ServicePackageOrganizationDto {
    private long serviceId;

    private String code;

    private String name;

    private Integer duration;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate startDate;

    private UsageStatus usageStatus;

    private CalculatorMethod calculatorMethod;
}
