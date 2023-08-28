package com.vhc.ec.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDto implements Serializable {

    private int id;

    private String name;

    private String email;

    private String phone;

    private short status;

    @JsonProperty("organization_id")
    private int organizationId;

    @JsonProperty("type_id")
    private int typeId;
    
    @JsonProperty("organization_change")
    private int organizationChange;
}
