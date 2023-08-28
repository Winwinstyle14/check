package com.vhc.ec.contract.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignatureInfoDto {
    private String quote;

    private String positionSignature;

    private String signer;

    private String signOn;

    private String location;

    private String reason;

    private Boolean isModified;

    private String durationValidCertificate;

    private String deptGrantCert;

    private String startDateCert;

    private String endDateCert;

    private Boolean signOnValidTime;

    private Boolean isRevoked;
}
