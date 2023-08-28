package com.vhc.ec.auth.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@ToString
public class ValidateTokenResponseDto implements Serializable {

    private boolean success;

    private int type;

    private CustomerDto customer;

}
