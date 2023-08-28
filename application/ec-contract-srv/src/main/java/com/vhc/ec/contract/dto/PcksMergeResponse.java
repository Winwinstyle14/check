package com.vhc.ec.contract.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PcksMergeResponse {
    private String hexDataSigned;

    private String base64Data;

    private String message;
}
