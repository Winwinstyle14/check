package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class TemplateShareDto implements Serializable {
    private Integer id;

    @NotBlank
    private String email;

    private int status;

    @JsonProperty("contract_id")
    private int contractId;

    @JsonProperty("organization_id")
    private int organizationId;

    @JsonProperty("customer_id")
    private int customerId;

    @JsonProperty("organization_name")
    private String orgName;
}
