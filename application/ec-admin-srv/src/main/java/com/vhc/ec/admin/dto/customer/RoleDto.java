package com.vhc.ec.admin.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class RoleDto {
    private Integer id;

    private String name;

    private String code;

    private String description;

    private int status;

    @JsonProperty("organization_id")
    private int organizationId;

    private Collection<PermissionDto> permissions;
}
