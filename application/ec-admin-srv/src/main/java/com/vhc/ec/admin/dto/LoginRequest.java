package com.vhc.ec.admin.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Địa chỉ email không được để trống")
    private String email;

    @NotBlank(message = "Mật khẩu đăng nhập không được để trống")
    private String password;
}
