package com.vhc.ec.auth.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class AdminLoginRequest {
    @NotBlank(message = "Địa chỉ email không được để trống")
    private String email;

    @NotBlank(message = "Mật khẩu đăng nhập không được để trống")
    private String password;
}
