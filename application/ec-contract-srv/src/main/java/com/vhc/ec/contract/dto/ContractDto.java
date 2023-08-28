package com.vhc.ec.contract.dto;

import static com.vhc.ec.contract.definition.DataFormat.TIMESTAMP_FORMAT;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.ws.rs.DefaultValue;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
public class ContractDto implements Serializable {

    private int id;

    @Length(max = 500, message = "Name '${validatedValue}' must be less than {max} characters long")
    private String name;

    @Length(max = 200, message = "Code '${validatedValue}' must be less than {max} characters long")
    private String code;

    @JsonProperty("contract_no")
    @Length(max = 200, message = "Contract no. '${validatedValue}' must be less than {max} characters long")
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
    private Integer typeId;

    @JsonProperty("is_template")
    @DefaultValue("false")
    private boolean isTemplate;

    @JsonProperty("status")
    private int status;

    @JsonProperty("organization_id")
    private Integer organizationId;

    private String notes;

    @JsonProperty("ui_config")
    private Map<String, Object> uiConfig;

    @JsonProperty("created_by")
    private Integer createdBy;

    @JsonProperty("created_at")
    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    private Date createdAt;

    private Set<ReferenceDto> refs;

    private Collection<ParticipantDto> participants;

    @JsonProperty("reason_reject")
    private String reasonReject;

    private Date cancelDate;

    @JsonProperty("template_contract_id")
    private Integer templateContractId; 
    
    @JsonProperty("release_state")
    private String releaseState;
    
    public String getReleaseState() {
        Date now = new Date();
        now = DateUtils.truncate(now, Calendar.DATE);

        if (now.after(DateUtils.truncate(signTime, Calendar.DATE))) {
            return "HET_HIEU_LUC";
        }

        return "CON_HIEU_LUC";
    }
    
    @JsonProperty("ceca_push")
    private Integer cecaPush;
    
    @JsonProperty("ceca_status")
    private Integer cecaStatus;
    
    @JsonProperty("contract_expire_time")
    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    private Date contractExpireTime;

    @JsonProperty("contract_uid")
    private String contractUid;
}
