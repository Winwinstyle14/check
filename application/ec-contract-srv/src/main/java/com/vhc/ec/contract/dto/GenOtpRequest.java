package com.vhc.ec.contract.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenOtpRequest {
    private int contractId;

    private String phone;
}
