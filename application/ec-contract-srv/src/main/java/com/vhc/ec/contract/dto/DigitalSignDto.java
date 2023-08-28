package com.vhc.ec.contract.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Thông tin hợp đồng ký số
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class DigitalSignDto implements Serializable {
    private String name;
    private String content;
}
