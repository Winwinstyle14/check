package com.vhc.ec.customer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class CreatePasswordResetRequest {

    private String email;

    private String token;

    private String username;
}
