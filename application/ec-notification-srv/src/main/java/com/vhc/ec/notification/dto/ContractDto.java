package com.vhc.ec.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.DefaultValue;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.vhc.ec.notification.definition.DataFormat.TIMESTAMP_FORMAT;

/**
 * Đối tượng lưu trữ thông tin của hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractDto implements Serializable {

    private int id;

    @NotBlank(message = "Name is mandatory")
    @Length(max = 191, message = "Name '${validatedValue}' must be less than {max} characters long")
    private String name;

    @NotBlank(message = "Code is mandatory")
    @Length(max = 31, message = "Code '${validatedValue}' must be less than {max} characters long")
    private String code;

    @JsonProperty("contract_no")
    @NotBlank(message = "Contract no. is mandatory")
    @Length(max = 191, message = "Contract no. '${validatedValue}' must be less than {max} characters long")
    private String contractNo;

    @JsonProperty("sign_time")
    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    private Date signTime;

    @JsonProperty("sign_order")
    @DefaultValue("1")
    private int signOrder;

    @JsonProperty("alias_url")
    private String aliasUrl;

    @JsonProperty("ref_id")
    private Integer refId;

    @JsonProperty("type_id")
    private int typeId;

    @JsonProperty("is_template")
    @DefaultValue("false")
    private boolean isTemplate;

    @JsonProperty("status")
    private int status;

    private String notes;

    @JsonProperty("created_by")
    private Integer createdBy;

    @JsonProperty("created_at")
    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    private Date createdAt;

    private Set<ReferenceDto> refs;

    private List<ParticipantDto> participants;

    @JsonProperty("reason_reject")
    private String reasonReject;
}
