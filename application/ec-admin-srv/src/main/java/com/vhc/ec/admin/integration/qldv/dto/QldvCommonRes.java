package com.vhc.ec.admin.integration.qldv.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QldvCommonRes {
    private int status;

    private String message;

    private String data = "";
}
