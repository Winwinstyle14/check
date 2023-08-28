package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Đối tượng lưu trữ thông tin loại hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@ToString
public class TypeDto implements Serializable {

    private Integer id;

    @NotBlank(message = "Name is mandatory")
    @Length(max = 255, message = "Name '${validatedValue}' must be less than {max} characters long")
    private String name;

    @NotBlank(message = "Code is mandatory")
    @Length(max = 63, message = "Code '${validatedValue}' must be less than {max} characters long")
    private String code;

    @JsonProperty("organization_id")
    private int organizationId;

    private int ordering;

    private int status;

    @JsonProperty("ceca_push")
    private Integer cecaPush;
}
