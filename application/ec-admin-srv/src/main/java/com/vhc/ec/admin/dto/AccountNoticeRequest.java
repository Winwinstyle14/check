package com.vhc.ec.admin.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AccountNoticeRequest {
    private String name;

    private String email;

    private String phone;

    private String accessCode;

    private String userType;
}
