package com.vhc.ec.customer.dto;

import lombok.Data;

@Data
public class TokenDto {

    private String username;
    private String tokenType;
    private String token;

    public TokenDto(String username, String jwtToken) {
        this.username = username;
        this.tokenType = "Bearer";
        this.token = jwtToken;
    }
}
