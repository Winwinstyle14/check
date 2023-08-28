package com.vhc.ec.customer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

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

    @NotBlank(message = "Phone is mandatory")
    @Length(max = 15, message = "Phone '${validatedValue}' must be less than {max} characters long")
    private String phone;

    @Length(max = 15, message = "Phone sign '${validatedValue}' must be less than {max} characters long")
    @JsonProperty("phone_sign")
    private String phoneSign;

    @JsonProperty("phone_tel")
    private Integer phoneTel;

    @JsonProperty("sign_image")
    private List<SignImage> signImage;

    @JsonProperty("hsm_name")
    private String hsmName;

    @JsonFormat(pattern = "yyyy/MM/dd", timezone = "Asia/Ho_Chi_Minh")
    private Date birthday;

    private int status;

    @JsonProperty("role_id")
    private Integer roleId;

    @JsonProperty("organization_id")
    private int organizationId;
    
    @JsonProperty("organization_change")
    private int organizationChange;
    
    @JsonProperty("tax_code")
    private String taxCode;
    
    @JsonProperty("hsm_pass")
    private String hsmPass;

    private RoleDto role;

    private OrganizationDto organization;

    @Getter
    @Setter
    @ToString
    public static class SignImage implements Serializable {
        private String bucket;
        private String path;
        @JsonProperty("presigned_url")
        private String presignedUrl;
    }
}
