package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import static com.vhc.ec.contract.definition.DataFormat.TIMESTAMP_FORMAT;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class ContractResponse implements Serializable {
    private int id;
    private String name;
    private String code;
    private int status;

    @JsonProperty("contract_no")
    private String contractNo;

    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    @JsonProperty("from_at")
    private Date fromAt;

    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    @JsonProperty("due_at")
    private Date dueAt;

    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    @JsonProperty("sign_time")
    private Date signTime;

    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    @JsonProperty("created_time")
    private Date createdAt;

    @JsonProperty("participants")
    private Collection<ParticipantResponseDto> participants;
    
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
}
