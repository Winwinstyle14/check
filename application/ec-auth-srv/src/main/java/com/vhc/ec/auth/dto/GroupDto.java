package com.vhc.ec.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author: VHC JSC
 * @version: 1.0
 * @since: 1.0
 */
@Data
@ToString
@NoArgsConstructor
public class GroupDto implements Serializable {

    private Integer id;

    @NotBlank(message = "Name is mandatory")
    @Length(max = 63, message = "Name '${validatedValue}' must be less than {max} characters long")
    private String name;

    @Min(0)
    @Max(1)
    private int status;
}
