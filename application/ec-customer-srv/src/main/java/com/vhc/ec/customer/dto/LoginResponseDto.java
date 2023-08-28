package com.vhc.ec.customer.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@Builder
@ToString
public class LoginResponseDto implements Serializable {
    private final boolean success;
    private final String code;
    private final CustomerDto customer;
}
