package com.vhc.ec.customer.dto;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OrganizationCodeUniqueRequest implements Serializable {

    @NotBlank
    private String code;
    
    @NotNull
    @JsonProperty("org_id_cha")
    private Integer orgIdCha;
     
    @JsonProperty("org_id_con")
    private Integer orgIdCon;
}
