package com.vhc.ec.customer.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AccountNoticeRequest {

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Email is mandatory")
    private String email;

    private String phone;

    private String accessCode;
}
