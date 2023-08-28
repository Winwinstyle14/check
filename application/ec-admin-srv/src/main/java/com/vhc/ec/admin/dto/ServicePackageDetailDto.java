package com.vhc.ec.admin.dto;

import com.vhc.ec.admin.constant.BaseStatus;
import com.vhc.ec.admin.constant.CalculatorMethod;
import com.vhc.ec.admin.constant.ServiceType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServicePackageDetailDto {
    private long id;

    private String code;

    private String name;

    private long totalBeforeVAT;

    private long totalAfterVAT;

    private CalculatorMethod calculatorMethod;

    private ServiceType type;

    private Integer duration;

    private Integer numberOfContracts;

    private String description;

    private BaseStatus status;
}
