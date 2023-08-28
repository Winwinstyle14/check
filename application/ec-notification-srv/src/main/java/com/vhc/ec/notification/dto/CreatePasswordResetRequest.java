package com.vhc.ec.notification.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class CreatePasswordResetRequest {

    @NotBlank(message = "Email is mandatory")
    private String email;

    private String phone;

    @NotBlank(message = "Token is mandatory")
    private String token;

    private String username;
}
