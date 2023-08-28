package com.vhc.ec.contract.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Thông tin đăng nhập của khách hàng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class LoginRequest implements Serializable {

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private Integer contractId;

}
