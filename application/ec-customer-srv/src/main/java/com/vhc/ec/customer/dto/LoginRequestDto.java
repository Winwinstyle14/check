package com.vhc.ec.customer.dto;

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
public class LoginRequestDto implements Serializable {

    @NotBlank(message = "Email is mandatory")
    @Length(max = 191, message = "Email '${validatedValue}' must be less than {max} characters long")
    private String email;

    @NotBlank(message = "Password is mandatory")
    private String password;
}
