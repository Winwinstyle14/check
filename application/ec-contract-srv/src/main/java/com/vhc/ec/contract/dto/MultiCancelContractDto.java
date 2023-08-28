package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class MultiCancelContractDto {
    @JsonProperty("contract_ids")
    private List<Integer> contractIds;

    @Length(max = 600)
    @NotBlank
    private String reason;
}
