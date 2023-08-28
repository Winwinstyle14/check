package com.vhc.ec.admin.dto;

import com.vhc.ec.admin.constant.BaseStatus;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class SaveUserDto {
    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @NotBlank
    private String phone;

    @NotNull
    private BaseStatus status;

    @NotEmpty
    private List<PermissionDto> permissions;
}
