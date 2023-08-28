package com.vhc.ec.admin.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Setter
@Getter
public class ServiceExpiredRequest {
    @NotBlank
    private String org;

    @NotBlank
    private String service;

    @NotEmpty
    List<String> emails;
}
