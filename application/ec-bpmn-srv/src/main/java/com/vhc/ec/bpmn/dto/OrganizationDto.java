package com.vhc.ec.bpmn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class OrganizationDto implements Serializable {

    private Integer id;

    @NotBlank(message = "Name is mandatory")
    @Length(max = 600, message = "Name '${validatedValue}' must be less than {max} characters long")
    private String name;

    @JsonProperty("short_name")
    @NotBlank(message = "Short name is mandatory")
    @Length(max = 63, message = "Short name '${validatedValue}' must be less than {max} characters long")
    private String shortName;

    @NotBlank(message = "Code is mandatory")
    @Length(max = 63, message = "Code '${validatedValue}' must be less than {max} characters long")
    private String code;

    @NotBlank(message = "Email is mandatory")
    @Length(max = 255, message = "Email '${validatedValue}' must be less than {max} characters long")
    private String email;

    @NotBlank(message = "Phone is mandatory")
    @Length(max = 15, message = "Phone '${validatedValue}' must be less than {max} characters long")
    private String phone;

    @Length(max = 15, message = "Fax '${validatedValue}' must be less than {max} characters long")
    private String fax;

    @JsonProperty("parent_id")
    private Integer parentId;

    private String path;

    private String brandName;

    private String smsUser;

    private String smsPass;

    private String smsSendMethor;
}
