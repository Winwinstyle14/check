package com.vhc.ec.bpmn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class CustomerDto implements Serializable {

    private Integer id;

    @NotBlank(message = "Name is mandatory")
    @Length(max = 600, message = "Name '${validatedValue}' must be less than {max} characters long")
    private String name;

    @NotBlank(message = "Email is mandatory")
    @Length(max = 191, message = "Email '${validatedValue}' must be less than {max} characters long")
    private String email;

    @NotBlank(message = "Password is mandatory")
    private String password;

    @NotBlank(message = "Phone is mandatory")
    @Length(max = 15, message = "Phone '${validatedValue}' must be less than {max} characters long")
    private String phone;

    @NotBlank(message = "Phone sign is mandatory")
    @Length(max = 15, message = "Phone sign '${validatedValue}' must be less than {max} characters long")
    @JsonProperty("phone_sign")
    private String phoneSign;

    @JsonProperty("phone_tel")
    private short phoneTel;

    @JsonFormat(pattern = "yyyy/MM/dd")
    private Date birthday;

    private short status;

    @JsonProperty("type_id")
    @Min(1)
    private int typeId;

    @JsonProperty("organization_id")
    @Min(1)
    private int organizationId;
}
