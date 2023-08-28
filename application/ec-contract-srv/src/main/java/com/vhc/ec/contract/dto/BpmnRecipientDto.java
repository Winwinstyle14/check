package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vhc.ec.contract.definition.ParticipantType;
import com.vhc.ec.contract.definition.RecipientRole;
import com.vhc.ec.contract.entity.Participant;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Date;

@Data
@NoArgsConstructor
public class BpmnRecipientDto implements Comparable<BpmnRecipientDto> {
    private Integer id;

    private String name;

    private String email;

    private String phone;

    private int role;

    private String username;

    private int ordering;

    private int status;

    private String reasonReject;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "Asia/Saigon")
    @JsonProperty("process_at")
    private Date processAt;

    private Collection<SignTypeDto> signType;

    @JsonIgnore
    private int participantOrder;

    private String participantName;

    private int participantType;

    @Override
    public int compareTo(BpmnRecipientDto other) {
        // nguoi dieu phoi luon dung dau
        if (role == RecipientRole.COORDINATOR.getDbVal() && other.getRole() != role) {
          return -1;
        }

        if (participantOrder != other.getParticipantOrder()) {
            return participantOrder - other.getParticipantOrder();
        }

        if (role != other.getRole()) {
            return role - other.getRole();
        }

        return ordering - other.getOrdering();
    }
}
