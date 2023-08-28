package com.vhc.ec.contract.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class MultiUsbTokenSignReq {
    private int fieldId;

    @NotNull
    private DigitalSignDto digitalSign;
}
