package com.vhc.ec.customer.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PasswordRecoverDto {

    private String email;
    private String password;
    private String token;
}
