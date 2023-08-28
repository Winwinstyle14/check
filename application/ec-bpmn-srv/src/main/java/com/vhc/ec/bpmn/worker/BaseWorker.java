package com.vhc.ec.bpmn.worker;

import com.vhc.ec.bpmn.definition.RecipientRole;
import com.vhc.ec.bpmn.dto.*;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
public abstract class BaseWorker {

    /**
     * Logging Bpmn activated job
     *
     * @param job
     */
    public void logging(ActivatedJob job) {

        log.info("Activated job: [type: {}, key: {}, element: {}, instance: {}] - [deadline: {}] - [headers: {}] - [variables: {}]",
                job.getType(),
                job.getKey(),
                job.getElementId(),
                job.getProcessInstanceKey(),
                Instant.ofEpochMilli(job.getDeadline()),
                job.getCustomHeaders(),
                job.getVariables());
    }

    /**
     * Tạo thông tin object để gửi thông bao cho người dùng
     *
     * @param contractDto
     * @param participant
     * @param recipientDto
     * @param approveType
     * @param currentRecipient
     * @param customerDto nguoi tao hop dong
     * @param organizationDto
     * @return
     */
    public SignFlowNotifyRequest getSignFlowNotifyRequest(ContractDto contractDto,
                                                          ParticipantDto participant,
                                                          RecipientDto recipientDto,
                                                          int approveType,
                                                          RecipientDto currentRecipient,
                                                          CustomerDto customerDto,
                                                          OrganizationDto organizationDto
    ) {

        SignFlowNotifyRequest request = new SignFlowNotifyRequest();

        request.setActionType(recipientDto.getRole());
        request.setApproveType(approveType);

        request.setContractId(contractDto.getId());
        request.setContractName(contractDto.getName());
        request.setContractUrl("" + contractDto.getId());
        request.setContractNotes(contractDto.getNotes());

        request.setAccessCode(recipientDto.getPassword());

        if (recipientDto.getPassword() != null && !recipientDto.getPassword().equals("")) {
            request.setLoginType("1");
        }

        request.setParticipantName(participant.getName());

        request.setRecipientId("" + recipientDto.getId());
        request.setRecipientName(recipientDto.getName());
        request.setRecipientEmail(recipientDto.getEmail());
        request.setRecipientPhone(recipientDto.getPhone());

        // Thong tin tu choi
        if (currentRecipient != null) {
            request.setReasonReject(currentRecipient.getReasonReject());
            request.setRecipientReject(currentRecipient.getName());
        }

        request.setSenderName(customerDto.getName());
        request.setSenderParticipant(organizationDto.getName());
        request.setLoginBy(recipientDto.getLoginBy());
        request.setOrgId(contractDto.getOrganizationId());
        request.setBrandName(organizationDto.getBrandName());
        request.setSmsUser(organizationDto.getSmsUser());
        request.setSmsPass(organizationDto.getSmsPass());
        request.setSmsSendMethor(organizationDto.getSmsSendMethor());
        request.setContractUid(contractDto.getContractUid());

        return request;
    }

    /**
     * @param contractDto
     * @param approveType
     * @param currentRecipient
     * @param customerDto
     * @param organizationDto
     * @return
     */
    public SignFlowNotifyRequest getSignFlowNotifyCustomerRequest(ContractDto contractDto,
                                                                  int approveType,
                                                                  RecipientDto currentRecipient,
                                                                  CustomerDto customerDto,
                                                                  OrganizationDto organizationDto
    ) {

        SignFlowNotifyRequest request = new SignFlowNotifyRequest();

        request.setActionType(currentRecipient.getRole());
        request.setApproveType(approveType);

        request.setContractId(contractDto.getId());
        request.setContractName(contractDto.getName());
        request.setContractUrl("" + contractDto.getId());
        request.setContractNotes(contractDto.getNotes());

        request.setRecipientName(customerDto.getName());
        request.setRecipientEmail(customerDto.getEmail());
        request.setRecipientPhone(customerDto.getPhone());

        // Thong tin tu choi
        if (currentRecipient != null) {
            request.setReasonReject(currentRecipient.getReasonReject());
            request.setRecipientReject(currentRecipient.getName());
        }

        request.setSenderName(customerDto.getName());
        request.setSenderParticipant(organizationDto.getName());
        // nguoi tao hop dong gui thong bao qua email
        request.setLoginBy("email");
        request.setOrgId(contractDto.getOrganizationId());
        request.setBrandName(organizationDto.getBrandName());
        request.setSmsUser(organizationDto.getSmsUser());
        request.setSmsPass(organizationDto.getSmsPass());
        request.setSmsSendMethor(organizationDto.getSmsSendMethor());
        request.setContractUid(contractDto.getContractUid());

        return request;
    }

    /**
     * Lay nguoi dang thuc hien xu ly HD
     *
     * @param contractDto
     * @return RecipientDto
     */
    public RecipientDto getCurrentRecipient(ContractDto contractDto, int recipientId) {

        for (ParticipantDto participant : contractDto.getParticipants()) {

            List<RecipientDto> recipients = participant.getRecipients();
            for (RecipientDto recipientDto : recipients) {

                // la nguoi thuc hien
                if (recipientDto.getId() == recipientId) {

                    return recipientDto;
                }
            }
        }

        return null;
    }

    /**
     * Lay to chuc dang xu ly HD
     *
     * @param contractDto
     * @return RecipientDto
     */
    public ParticipantDto getCurrentParticipant(ContractDto contractDto, int recipientId) {

        for (ParticipantDto participant : contractDto.getParticipants()) {

            List<RecipientDto> recipients = participant.getRecipients();
            for (RecipientDto recipientDto : recipients) {

                // la nguoi thuc hien
                if (recipientDto.getId() == recipientId) {

                    return participant;
                }
            }
        }

        return null;
    }

    /**
     * Kiem tra tat ca nguoi review da xu ly hay chua
     *
     * @param contractDto
     * @param currentParticipant
     * @return
     */
    protected boolean checkReviewerIsProcessed(ContractDto contractDto, ParticipantDto currentParticipant) {

        // To chuc cua nguoi dang xu ly
        for (RecipientDto recipientDto : currentParticipant.getRecipients()) {

            // Con bat ky recipient nao role < SIGN chua xu ly
            if (recipientDto.getRole() < RecipientRole.SIGNER.getDbVal().intValue() && recipientDto.getProcessAt() == null) {
                return false;
            }
        }

        // Cac to chuc khac co cung thu tu xu ly
        for (ParticipantDto participantDto : contractDto.getParticipants()) {
            if (participantDto.getOrdering() == currentParticipant.getOrdering()) {

                for (RecipientDto recipientDto : participantDto.getRecipients()) {

                    // Con bat ky recipient nao role < SIGN chua xu ly
                    if (recipientDto.getRole() < RecipientRole.SIGNER.getDbVal().intValue() && recipientDto.getProcessAt() == null) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Kiem tra co to chuc nao cung thu tu xu ly khong
     *
     * @param contractDto
     * @param currentParticipant
     * @return
     */
    protected boolean participantEqualOrder(ContractDto contractDto, ParticipantDto currentParticipant) {

        for (ParticipantDto participantDto : contractDto.getParticipants()) {

            // co to chuc khac co cung thu tu Ordering
            if (participantDto.getOrdering() == currentParticipant.getOrdering() && participantDto.getId() != currentParticipant.getId()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Kiem tra tat ca nguoi signer da xu ly hay chua
     *
     * @param contractDto
     * @param currentParticipant
     * @return
     */
    protected boolean checkSignerIsProcessed(ContractDto contractDto, ParticipantDto currentParticipant) {

        // To chuc cua nguoi dang xu ly
        for (RecipientDto recipientDto : currentParticipant.getRecipients()) {

            // Con bat ky recipient nao role = SIGN chua xu ly
            if (recipientDto.getRole() == RecipientRole.SIGNER.getDbVal().intValue() && recipientDto.getProcessAt() == null) {
                return false;
            }
        }

        // Cac to chuc khac co cung thu tu xu ly
        for (ParticipantDto participantDto : contractDto.getParticipants()) {
            if (participantDto.getOrdering() == currentParticipant.getOrdering()) {

                for (RecipientDto recipientDto : participantDto.getRecipients()) {

                    // Con bat ky recipient nao role < SIGN chua xu ly
                    if (recipientDto.getRole() == RecipientRole.SIGNER.getDbVal().intValue() && recipientDto.getProcessAt() == null) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
