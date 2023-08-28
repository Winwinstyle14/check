package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author T.H.A, LTD
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class TypeCodeUniqueRequest implements Serializable {

    @NotNull
    @JsonProperty("organization_id")
    private Integer organizationId;

    @NotBlank
    private String code;

}
