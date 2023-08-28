package com.vhc.ec.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AdminTokenResponse {
    private String token;

    private UserViewDto user;

    private String code;
}
