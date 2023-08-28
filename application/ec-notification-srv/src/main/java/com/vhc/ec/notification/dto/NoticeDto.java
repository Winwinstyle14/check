package com.vhc.ec.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

import static com.vhc.ec.notification.definition.DataFormat.TIMESTAMP_FORMAT;

/**
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class NoticeDto {

    private int id;

    @JsonProperty("contract_id")
    private int contractId;

    @JsonProperty("message_id")
    private int messageId;

    @JsonProperty("message_code")
    private String messageCode;

    @JsonProperty("notice_name")
    private String noticeName;

    @JsonProperty("notice_content")
    private String noticeContent;

    @JsonProperty("notice_url")
    private String noticeUrl;

    @JsonProperty("email")
    private String email;

    @JsonProperty("notice_date")
    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    private Date noticeDate;

    private int status;

    @JsonProperty("created_at")
    @JsonFormat(pattern = TIMESTAMP_FORMAT)
    private Date createdAt;
}
