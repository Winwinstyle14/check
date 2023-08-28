package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class BpmnFlowRes {
    private BpmnRecipientDto createdBy;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "Asia/Saigon")
    private Date createdAt;

    private String reasonCancel;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "Asia/Saigon")
    private Date cancelDate;

    private List<BpmnRecipientDto> recipients;

    private int contractStatus;
}
