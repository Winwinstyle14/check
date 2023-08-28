package com.vhc.ec.customer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@ToString
public class RoleDto implements Serializable {

    private Integer id;

    private String name;

    private String code;

    private String description;

    private int status;

    @JsonProperty("organization_id")
    private int organizationId;

    private Collection<PermissionDto> permissions;
}
