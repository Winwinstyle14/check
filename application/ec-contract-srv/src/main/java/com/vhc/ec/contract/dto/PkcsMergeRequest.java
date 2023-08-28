package com.vhc.ec.contract.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PkcsMergeRequest {
    private int contractId;

    private String signature;

    private String hexDigestTempFile;

    private String filedName;

    private String cert;

    private String isTimestamp;
}
