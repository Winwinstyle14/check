package com.vhc.ec.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@ToString
@JsonPropertyOrder({"code", "type", "access_token", "customer"})
public class TokenResponse implements Serializable {

    private final String type;
    @JsonProperty("access_token")
    private final String accessToken;
    private final Customer customer;
    private String code;

    @Data
    @Builder
    @AllArgsConstructor
    @ToString
    public static class Customer {

        private final int type;

        private final Map<String, Object> info;

    }
}
