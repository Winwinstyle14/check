package com.vhc.ec.customer.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author T.H.A, LTD
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class RoleNameUniqueRequest implements Serializable {

    @NotBlank
    private String name;
    
    @NotNull
	@JsonProperty("organization_id")
	private Integer organizationId;

}
