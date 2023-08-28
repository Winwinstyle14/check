package com.vhc.ec.contract.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author T.H.A, LTD
 * @version 1.0
 * @since 1.0
 */
@Data
@ToString
public class ContractNameUniqueRequest implements Serializable {

    @NotBlank
    private String name;

}
