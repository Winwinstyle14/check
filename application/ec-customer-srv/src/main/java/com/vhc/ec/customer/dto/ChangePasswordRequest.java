package com.vhc.ec.customer.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ChangePasswordRequest {
    private String password;
    private String newPassword;
}
