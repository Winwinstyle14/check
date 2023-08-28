package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import static com.vhc.ec.contract.definition.DataFormat.TIMESTAMP_FORMAT;

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
public class TemplateContractDto implements Serializable {

    private int id;

    @NotBlank(message = "Name is mandatory")
    @Length(max = 500, message = "Name '${validatedValue}' must be less than {max} characters long")
    private String name;

    @NotBlank(message = "Code is mandatory")
    @Length(max = 200, message = "Code '${validatedValue}' must be less than {max} characters long")
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

    private Collection<TemplateParticipantDto> participants;

    public String getReleaseState() {
        Date now = new Date();
        now = DateUtils.truncate(now, Calendar.DATE);

        if (now.before(DateUtils.truncate(startTime, Calendar.DATE))) {
            return "CHUA_CO_HIEU_LUC";
        } else if (now.after(DateUtils.truncate(endTime, Calendar.DATE))) {
            return "HET_HIEU_LUC";
        }

        return "CON_HIEU_LUC";
    }

    @JsonProperty("ceca_push")
    private Integer cecaPush;
}
