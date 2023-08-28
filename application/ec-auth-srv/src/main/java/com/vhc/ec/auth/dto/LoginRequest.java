package com.vhc.ec.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class LoginRequest implements Serializable {
    @NotBlank(message = "Địa chỉ email không được để trống")
    @Length(max = 191, message = "Địa chỉ email '${validatedValue}' cần phải ít hơn {max} ký tự")
    private String email;

    @NotBlank(message = "Mật khẩu đăng nhập không được để trống")
    private String password;

    private int type;

    private Integer contractId;
}
