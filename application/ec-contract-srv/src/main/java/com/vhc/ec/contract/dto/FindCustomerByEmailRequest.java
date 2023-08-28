package com.vhc.ec.contract.dto;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@ToString
public class FindCustomerByEmailRequest implements Serializable {

    private final String email;

}
