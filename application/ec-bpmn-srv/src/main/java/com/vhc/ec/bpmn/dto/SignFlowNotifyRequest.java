package com.vhc.ec.bpmn.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class SignFlowNotifyRequest {

    @NotNull(message = "ActionType is mandatory")
    private int actionType;

    @NotNull(message = "ApproveType is mandatory")
    private int approveType;

    private int contractId;

    @NotBlank(message = "contractName is mandatory")
    private String contractName;

    private String contractOwner;

    @NotBlank(message = "contractUrl is mandatory")
    private String contractUrl;

    private String contractNotes;

    private String accessCode;

    private String participantName;

    private String recipientId;

    @NotBlank(message = "RecipientName is mandatory")
    private String recipientName;

    @NotBlank(message = "RecipientEmail is mandatory")
    private String recipientEmail;

    private String recipientPhone;

    private String reasonReject;

    private String recipientReject;

    private String senderName;

    private String senderParticipant;

    private String loginType;

    private String notificationCode;

    private String loginBy;

    private int orgId;

    private String brandName;

    private String smsUser;

    private String smsPass;

    @NotBlank
    private String smsSendMethor;

    @NotBlank
    private String contractUid;

    public String getLoginBy() {
        return loginBy == null ? "email" : loginBy;
    }
}
