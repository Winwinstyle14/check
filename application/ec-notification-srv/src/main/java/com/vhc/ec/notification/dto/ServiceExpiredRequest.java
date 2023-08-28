package com.vhc.ec.notification.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
public class ServiceExpiredRequest {
    @NotBlank
    private String org;

    @NotBlank
    private String service;

    @NotEmpty
    List<String> emails;
}
