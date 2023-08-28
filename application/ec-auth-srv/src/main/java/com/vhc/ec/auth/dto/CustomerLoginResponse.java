package com.vhc.ec.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CustomerLoginResponse implements Serializable {

    private boolean success;

    private String code;

    @JsonProperty("customer")
    private CustomerDto customer;
}
