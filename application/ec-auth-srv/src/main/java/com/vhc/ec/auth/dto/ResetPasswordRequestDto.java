package com.vhc.ec.auth.dto;

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
public class ResetPasswordRequestDto {

    @NotBlank(message = "Email address is mandatory")
    private String email;

}
