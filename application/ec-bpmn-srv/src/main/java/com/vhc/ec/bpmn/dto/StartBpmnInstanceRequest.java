package com.vhc.ec.bpmn.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class StartBpmnInstanceRequest {

    @NotNull(message = "ContractId is mandatory")
    private Long contractId;

    @NotNull(message = "ActionType is mandatory")
    private Integer actionType;

    @NotNull(message = "ApproveType is mandatory")
    private Integer approveType;

    @NotNull(message = "ParticipantId is mandatory")
    private Integer participantId;

    @NotNull(message = "RecipientId is mandatory")
    private Integer recipientId;
}
