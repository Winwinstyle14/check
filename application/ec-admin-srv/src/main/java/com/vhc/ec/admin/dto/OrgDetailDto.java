package com.vhc.ec.admin.dto;

import com.vhc.ec.admin.constant.CeCAPushMode;
import com.vhc.ec.admin.constant.OrgStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrgDetailDto {
    private int id;

    private String name;

    private String code;

    private String taxCode;

    private String shortName;

    private String address;

    private String email;

    private String representative;

    private String position;

    private String size;

    private String phone;

    private Integer numberOfContractsCanCreate;

    private Integer numberOfEkyc;

    private Integer numberOfSms;

    private OrgStatus status;

    private List<ServicePackageOrganizationDto> services;

    private CeCAPushMode ceCAPushMode;
}
