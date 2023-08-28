package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

import static com.vhc.ec.contract.definition.DataFormat.TIMESTAMP_FORMAT;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateContractCodeUniqueRequest {
    @NotBlank(message = "Code is mandatory")
    @Length(max = 31, message = "Code '${validatedValue}' must be less than {max} characters long")
    private String code;

    @NotBlank
    @JsonProperty("start_time")
    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    private Date startTime;

    @NotBlank
    @JsonProperty("end_time")
    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    private Date endTime;

    @NotNull
    @JsonProperty("organization_id")
    private Integer organizationId;
    
    @JsonProperty("id")
    private Integer contractId;
}
