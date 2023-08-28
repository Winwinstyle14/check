package com.vhc.ec.admin.dto;

import com.vhc.ec.admin.constant.BaseStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserViewDto {

    private long id;

    private String name;

    private String email;

    private String phone;

    private BaseStatus status;

    private List<PermissionDto> permissions;
}
