package com.vhc.ec.contract.dto.tp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vhc.ec.contract.dto.FieldUpdateRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Collection;

@Getter
@Setter
public class SignContractDto {

    private String mobile;

    private String networkCode;

    private String prompt;

    private String reason;

    // ky anh
    private Integer otp;

    private String signInfo;

    private String processAt;

    Collection<FieldUpdateRequest> fields;

    // hsm
    private String username;

    private String password;

    @JsonProperty("ma_dvcs")
    private String taxCode;

    private String password2;

    @NotBlank
    @JsonProperty("image_base64")
    private String imageBase64;

    // usb token
    private int fieldId;

    private String name;

    private String content;
}
