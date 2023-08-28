package com.vhc.ec.admin.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.vhc.ec.admin.constant.CeCAPushMode;
import com.vhc.ec.admin.constant.OrgStatus;

import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveOrgReq {
    @NotBlank
    private String name;

    @NotBlank
    private String code;

    @NotBlank
    private String taxCode;

    private String shortName;

    @NotBlank
    private String address;

    @NotBlank
    private String email;

    @NotBlank
    private String representative;

    @NotBlank
    private String position;

    @NotBlank
    private String size;

    @NotBlank
    private String phone;

    @NotNull
    private OrgStatus status;

    private CeCAPushMode ceCAPushMode;

    private LocalDate startLicense;

    private LocalDate endLicense;

    // so luong hop dong co the tao
    private int numberOfContractsCanCreate;

    private int numberOfEkyc;

    private int numberOfSms;

    // tong so luong hop dong trong cac goi so luong hop dong da dang ky
    private int totalContractsPurchased;

    private int totalSmsPurchased;

    private int totalEkycPurchased;

    private int totalPackagePurchased;
}
