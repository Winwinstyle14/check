package com.vhc.ec.contract.dto;

import static com.vhc.ec.contract.definition.DataFormat.TIMESTAMP_FORMAT;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class TemplateContractModifileDto implements Serializable {

    private int id;

    @NotBlank(message = "Name is mandatory")
    @Length(max = 191, message = "Name '${validatedValue}' must be less than {max} characters long")
    private String name;

    @NotBlank(message = "Code is mandatory")
    @Length(max = 31, message = "Code '${validatedValue}' must be less than {max} characters long")
    private String code;

    @JsonProperty("start_time")
    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    private Date startTime;

    @JsonProperty("end_time")
    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    private Date endTime;

    @JsonProperty("type_id")
    private Integer typeId;

    @JsonProperty("status")
    private int status;

    @JsonProperty("organization_id")
    private Integer organizationId;

    @JsonProperty("created_by")
    private Integer createdBy;

    @JsonProperty("created_at")
    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    private Date createdAt;
}
