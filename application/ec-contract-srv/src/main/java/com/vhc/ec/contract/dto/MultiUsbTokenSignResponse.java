package com.vhc.ec.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultiUsbTokenSignResponse {
    private int fieldId;

    private MessageDto result;
}
