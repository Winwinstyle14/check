package com.vhc.ec.customer.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@ToString
public class PermissionDto implements Serializable {

    private Integer id;

    private String code;

    private int status;
}
