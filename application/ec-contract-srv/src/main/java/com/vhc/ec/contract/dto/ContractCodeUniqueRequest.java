package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author T.H.A, LTD
 * @version 1.0
 * @since 1.0
 */
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractCodeUniqueRequest {
    @NotBlank(message = "Code is mandatory")
    @Length(max = 100, message = "Code '${validatedValue}' must be less than {max} characters long")
    private String code;

    @NotNull
    @JsonProperty("organization_id")
    private Integer organizationId;
}
