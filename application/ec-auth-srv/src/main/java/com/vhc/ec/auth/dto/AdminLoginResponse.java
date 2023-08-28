package com.vhc.ec.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLoginResponse {
    private String code;

    private boolean success;

    private UserViewDto user;
}
