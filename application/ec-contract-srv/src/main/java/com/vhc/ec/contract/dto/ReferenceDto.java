package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ReferenceDto implements Serializable {

    @JsonProperty("ref_id")
    private int refId;

    @JsonProperty("contract_id")
    private Integer contractId;

    @JsonProperty("ref_name")
    private String refName;

}
