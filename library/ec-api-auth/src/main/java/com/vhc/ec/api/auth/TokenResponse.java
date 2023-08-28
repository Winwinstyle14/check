package com.vhc.ec.api.auth;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * Thông tin xác thực trả về cho người dùng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@ToString
public class TokenResponse implements Serializable {

    private boolean success;

    private CustomerUser customer;

}
