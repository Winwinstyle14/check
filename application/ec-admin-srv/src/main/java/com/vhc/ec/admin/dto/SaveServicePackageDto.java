package com.vhc.ec.admin.dto;

import com.vhc.ec.admin.constant.BaseStatus;
import com.vhc.ec.admin.constant.CalculatorMethod;
import com.vhc.ec.admin.constant.ServiceType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class SaveServicePackageDto {
    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotNull
    private long totalBeforeVAT;

    @NotNull
    private long totalAfterVAT;

    @NotNull
    private CalculatorMethod calculatorMethod;

    private ServiceType type;

    private Integer duration; // by month

    private Integer numberOfContracts;

    private Integer numberOfEkyc;

    private Integer numberOfSms;

    private String description;

    private BaseStatus status;
}
