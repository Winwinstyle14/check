package com.vhc.ec.contract.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

@Getter
@Setter
@ToString
public class ProcessApprovalDto {
    private Integer otp;

    private String signInfo;

    private String processAt;

    Collection<FieldUpdateRequest> fields;
}
