package com.vhc.ec.customer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class FindCustomerByEmailRequest implements Serializable {

    private String email;

}
