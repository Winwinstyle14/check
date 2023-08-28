package com.vhc.ec.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * Đối tượng lưu trữ thông tin đăng nhập của người dùng,
 * trong trường hợp không phải là khách hàng đã đăng ký tài khoản trên hệ thống
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@ToString
public class LoginResponse implements Serializable {

    private final boolean success;

    private final RecipientResponse recipient;

    @Data
    @Builder
    @AllArgsConstructor
    @ToString
    public static class RecipientResponse implements Serializable {
        private final int id;
        private final String name;
        private final String email;
        private final String username;
    }

}
