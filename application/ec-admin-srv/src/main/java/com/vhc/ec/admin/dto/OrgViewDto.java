package com.vhc.ec.admin.dto;

import com.vhc.ec.admin.constant.CeCAPushMode;
import com.vhc.ec.admin.constant.OrgStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrgViewDto {
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

    private OrgStatus status;

    private CeCAPushMode ceCAPushMode;

    // 1: email moi, 2: email thuoc to chuc, 3: email khong thuoc to chuc, 4: chua tao admin cho to chuc can tao admin
    private int codeInfo;

    public OrgViewDto(int codeInfo) {
        this.codeInfo = codeInfo;
    }
}
