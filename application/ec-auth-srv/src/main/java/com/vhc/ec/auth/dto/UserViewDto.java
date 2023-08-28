package com.vhc.ec.auth.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserViewDto {
    private long id;

    private String name;

    private String email;

    private String phone;

    private List<PermissionDto> permissions;
}
