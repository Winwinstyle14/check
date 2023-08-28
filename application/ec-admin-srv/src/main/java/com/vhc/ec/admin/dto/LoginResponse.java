package com.vhc.ec.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class LoginResponse {
    private String code;

    private boolean success;

    private UserViewDto user;
}
