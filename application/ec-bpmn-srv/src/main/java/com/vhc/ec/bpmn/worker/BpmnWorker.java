package com.vhc.ec.bpmn.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhc.ec.bpmn.definition.ContractStatus;
import com.vhc.ec.bpmn.definition.RecipientRole;
import com.vhc.ec.bpmn.definition.RecipientStatus;
import com.vhc.ec.bpmn.dto.*;
import com.vhc.ec.bpmn.service.ContractService;
import com.vhc.ec.bpmn.service.CustomerService;
import com.vhc.ec.bpmn.service.NotificationService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableZeebeClient
public class BpmnWorker extends BaseWorker {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    final ContractService contractService;
    final CustomerService customerService;
    final NotificationService notificationService;

    /**
     * Start HD
     *
     * @param client
     * @param job
     */
    @ZeebeWorker(type = "start-contract", name = "main-worker")
    public void startContract(final JobClient client, final ActivatedJob job) {
        Map<String, Object> zeebeVariables = new HashMap<>();
        String error = null;
        SignFlowNotifyRequest signFlowNotifyRequest;

        try {
            logging(job);
            int approveType = Integer.parseInt(job.getVariablesAsMap().get("approveType").toString());

            String contractJson = job.getVariablesAsMap().get("contract").toString();
            ContractDto contractDto = objectMapper.readValue(contractJson, ContractDto.class);

            log.info("contract.get: {}", contractDto);

            if (contractDto == null) {
                error = "Contract khong ton tai";
                return;
            }

            // Lay thong tin customer tao HD
            CustomerDto customerDto = customerService.getCustomer(contractDto.getCreatedBy());
            OrganizationDto organizationDto = customerService.getOrganization(contractDto.getCreatedBy());
            boolean findCoordinator = false;
            int minOrder = -1;

            // Gửi cho người điều phối trước
            for (ParticipantDto participant : contractDto.getParticipants()) {
                if (minOrder > -1 && participant.getOrdering() > minOrder) {
                    return;
                }

                List<RecipientDto> recipients = participant.getRecipients();
                for (RecipientDto recipientDto : recipients) {
                    if (recipientDto.getRole() == RecipientRole.COORDINATOR.getDbVal() && recipientDto.getStatus() == 0) {

                        if (minOrder == -1) {
                            minOrder = participant.getOrdering();
                        }

                        signFlowNotifyRequest = getSignFlowNotifyRequest(contractDto,
                                participant, recipientDto, approveType, null, customerDto, organizationDto);

                        // cap nhat trang thai dang xu ly
                        recipientDto.setStatus(RecipientStatus.PROCESSING.getDbVal());
                        contractService.changeRecipientProcessing(recipientDto.getId());

                        SignFlowNotifyResponse res = notificationService.sendSignFlowNotify(signFlowNotifyRequest);

                        log.info("notify, {}", res);
                        findCoordinator = true;
                    }
                }
            }

            if (findCoordinator) {
                return;
            }

            List<Integer> recipientSigner = new ArrayList<Integer>();
            boolean findRecipient = false;

            // start luong ky HD
            int signOrdering = 0;
            for (ParticipantDto participant : contractDto.getParticipants()) {
                // participant khong cung Ordering voi participant truoc do
                if (signOrdering != 0 && signOrdering != participant.getOrdering())
                    break;

                signOrdering = participant.getOrdering();

                List<RecipientDto> recipients = participant.getRecipients();

                // TODO kiem tra recipients

                for (RecipientDto recipientDto : recipients) {

                    // TODO: check cac recipient cung ordering
                    if (recipientDto.getRole() == RecipientRole.REVIEWER.getDbVal().intValue()
                            && recipientDto.getOrdering() == 1) {

                        signFlowNotifyRequest = getSignFlowNotifyRequest(contractDto,
                                participant, recipientDto, approveType, null, customerDto, organizationDto);

                        // cap nhat trang thai dang xu ly
                        recipientDto.setStatus(RecipientStatus.PROCESSING.getDbVal());
                        contractService.changeRecipientProcessing(recipientDto.getId());

                        SignFlowNotifyResponse res = notificationService.sendSignFlowNotify(signFlowNotifyRequest);

                        log.info("[contract-{}] notify, {}", contractDto.getId() , res);

                        // da tim duoc nguoi xu ly
                        findRecipient = true;

                        // remove recipientSigner
                        if (recipientSigner.size() > 0) {
                            recipientSigner.clear();
                            log.info("Cleared recipientSigner list");
                        }

                    } else if (recipientDto.getRole() == RecipientRole.SIGNER.getDbVal()
                            && recipientDto.getOrdering() == 1) {
                        // add danh sach nguoi ky cho xu ly
                        if (!findRecipient) {
                            log.info("[contract-{}] add {} into recipientSigner", contractDto.getId(), recipientDto.getId());
                            recipientSigner.add(recipientDto.getId());
                        }
                    }
                } // end for participants
            }

            // Truong hop khong co bat ky nguoi xu ly nao truoc SIGNER
            if (recipientSigner.size() > 0) {
                log.info("[contract-{}] notify SIGNER: ", contractDto.getId());
                for (Integer recipientId : recipientSigner) {

                    for (ParticipantDto participant : contractDto.getParticipants()) {

                        List<RecipientDto> recipients = participant.getRecipients();
                        for (RecipientDto recipientDto : recipients) {

                            if (recipientDto.getId() == recipientId.intValue()) {

                                signFlowNotifyRequest = getSignFlowNotifyRequest(contractDto,
                                        participant, recipientDto, approveType, null, customerDto, organizationDto);

                                // cap nhat trang thai dang xu ly
                                recipientDto.setStatus(RecipientStatus.PROCESSING.getDbVal());
                                contractService.changeRecipientProcessing(recipientDto.getId());

                                SignFlowNotifyResponse res = notificationService.sendSignFlowNotify(signFlowNotifyRequest);

                                log.info("[contract-{}] notify, {}", res, contractDto.getId());
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {

            log.error("error", e);
            error = ExceptionUtils.getFullStackTrace(e);
        } finally {

            if (error != null) {
                zeebeVariables.put("error", error);
            }
            client.newCompleteCommand(job.getKey()).variables(zeebeVariables).send().join();
        }
    }

    /**
     * Tu choi HD
     *
     * @param client
     * @param job
     */
    @ZeebeWorker(type = "reject-contract", name = "main-worker")
    public void rejectService(final JobClient client, final ActivatedJob job) {

        logging(job);

        Map<String, Object> zeebeVariables = new HashMap<>();
        String error = null;

        try {
            int approveType = Integer.parseInt(job.getVariablesAsMap().get("approveType").toString());
            int recipientId = Integer.parseInt(job.getVariablesAsMap().get("recipientId").toString());

            String contractJson = job.getVariablesAsMap().get("contract").toString();

            ContractDto contractDto = objectMapper.readValue(contractJson, ContractDto.class);

            // Cap nhat trang thai hop dong thanh REJECT
            ContractDto changeStatusResponse = contractService.changeStatus(contractDto.getId(), ContractStatus.REJECTED.getDbVal());

            log.info("Reject contract: " + changeStatusResponse);

            // Lay thong tin customer tao HD
            CustomerDto customerDto = customerService.getCustomer(contractDto.getCreatedBy());
            OrganizationDto organizationDto = customerService.getOrganization(contractDto.getCreatedBy());

            // Nguoi dang thuc hien
            RecipientDto currentRecipient = getCurrentRecipient(contractDto, recipientId);

            SignFlowNotifyRequest signFlowNotifyRequest;
            SignFlowNotifyResponse resFinish;

            // Gui thong bao den nguoi tao HD
            try {
                signFlowNotifyRequest = getSignFlowNotifyCustomerRequest(contractDto,
                        approveType, currentRecipient, customerDto, organizationDto);
                resFinish = notificationService.sendSignFlowNotify(signFlowNotifyRequest);
                log.info("notify, {}", resFinish);
            } catch (Exception e) {
                log.error("error", e);
            }

            // gui thong bao den tat ca nguoi tham gia HD
            for (ParticipantDto participant : contractDto.getParticipants()) {
                List<RecipientDto> recipients = participant.getRecipients();

                // TODO kiem tra recipients

                for (RecipientDto recipientDto : recipients) {

                    // gui thong bao tu choi den recipientDto
                    try {
                        signFlowNotifyRequest = getSignFlowNotifyRequest(contractDto,
                                participant, recipientDto, approveType, currentRecipient, customerDto, organizationDto);
                        // tao thong bao
                        resFinish = notificationService.sendSignFlowNotify(signFlowNotifyRequest);

                        log.info("notify, {}", resFinish);
                    } catch (Exception e) {
                        log.error("error", e);
                        error = ExceptionUtils.getFullStackTrace(e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("error", e);
            error = ExceptionUtils.getFullStackTrace(e);
        } finally {
            if (error != null) {
                zeebeVariables.put("error", error);
            }
            client.newCompleteCommand(job.getKey()).variables(zeebeVariables).send().join();
        }
    }

    /**
     * Dieu phoi HD
     *
     * @param client
     * @param job
     */
    @ZeebeWorker(type = "coordinator-contract", name = "main-worker")
    public void handleCoordinatorService(final JobClient client, final ActivatedJob job) {

        logging(job);

        Map<String, Object> zeebeVariables = new HashMap<>();
        String error = null;

        try {
            // get variables
            int contractId = Integer.parseInt(job.getVariablesAsMap().get("contractId").toString());
            int actionType = Integer.parseInt(job.getVariablesAsMap().get("actionType").toString());
            int approveType = Integer.parseInt(job.getVariablesAsMap().get("approveType").toString());
            int recipientId = Integer.parseInt(job.getVariablesAsMap().get("recipientId").toString());
            int participantId = Integer.parseInt(job.getVariablesAsMap().get("participantId").toString());
            String contractJson = job.getVariablesAsMap().get("contract").toString();

            ContractDto contractDto = objectMapper.readValue(contractJson, ContractDto.class);

            // dieu phoi?
            if (actionType != RecipientRole.COORDINATOR.getDbVal().intValue())
                return;

            // check truong hop uy quyen neu cac ben ky khac co order < hon chua xu ly het thi dung lai
            // Nguoi dang thuc hien
            ParticipantDto currentParticipantDto = getCurrentParticipant(contractDto, recipientId);

//            for (ParticipantDto participantDto : contractDto.getParticipants()) {
//
//                if (participantDto.getOrdering() < currentParticipantDto.getOrdering()) {
//
//                    List<RecipientDto> recipients = participantDto.getRecipients();
//                    for (RecipientDto recipientDto : recipients) {
//
//                        // chua xu ly
//                        if (recipientDto.getProcessAt() == null) {
//
//                            log.info("coordinator: recipient={},participant={} don't finish", recipientId);
//                            return;
//                        }
//                    }
//                }
//            }

            // xu ly nghiep vu
            error = processCoordinatorContract(contractDto, contractId, actionType, approveType, participantId, recipientId);
        } catch (Exception e) {
            log.error("error", e);

            error = ExceptionUtils.getFullStackTrace(e);
        } finally {
            if (error != null) {
                zeebeVariables.put("error", error);
            }
            client.newCompleteCommand(job.getKey()).variables(zeebeVariables).send().join();
        }
    }

    /**
     * Review HD
     *
     * @param client
     * @param job
     */
    @ZeebeWorker(type = "review-contract", name = "main-worker")
    public void reviewContract(final JobClient client, final ActivatedJob job) {

        Map<String, Object> zeebeVariables = new HashMap<>();
        String error = null;

        try {
            logging(job);
            // get variables
            int contractId = Integer.parseInt(job.getVariablesAsMap().get("contractId").toString());
            int actionType = Integer.parseInt(job.getVariablesAsMap().get("actionType").toString());
            int approveType = Integer.parseInt(job.getVariablesAsMap().get("approveType").toString());
            int recipientId = Integer.parseInt(job.getVariablesAsMap().get("recipientId").toString());
            int participantId = Integer.parseInt(job.getVariablesAsMap().get("participantId").toString());
            String contractJson = job.getVariablesAsMap().get("contract").toString();

            ContractDto contractDto = objectMapper.readValue(contractJson, ContractDto.class);

            log.info("Contract: {}", contractDto);

            // xu ly nghiep vu
            error = processReviewContract(contractDto, contractId, actionType, approveType, participantId, recipientId);
        } catch (Exception e) {

            log.error("error", e);
            error = ExceptionUtils.getFullStackTrace(e);
        } finally {
            if (error != null) {
                zeebeVariables.put("error", error);
            }
            client.newCompleteCommand(job.getKey()).variables(zeebeVariables).send().join();
        }
    }

    /**
     * Tim nguoi xu ly tiep theo cua Reviewer
     *
     * @param contractDto
     * @param contractId
     * @param actionType
     * @param approveType
     * @param participantId
     * @param recipientId
     * @return
     */
    private String processReviewContract(ContractDto contractDto, int contractId, int actionType, int approveType, int participantId, int recipientId) {
        log.info("[processReviewContract][contract-{}] recipient-{}", contractId, recipientId);
        String error = null;
        SignFlowNotifyRequest signFlowNotifyRequest;

        // kiem tra trang thai contract.status co khop voi actionType khong
        if (contractDto == null
                || (actionType == 0 && ContractStatus.CREATED.getDbVal().intValue() != contractDto.getStatus())        // khong phai trang thai CREATED
                || (actionType != 0 && ContractStatus.PROCESSING.getDbVal().intValue() != contractDto.getStatus())  // Khong phai trang thai PROCESSING
        ) {
            error = "Trang thai Contract khong hop le";
            return error;
        }

        // Lay thong tin customer tao HD
        CustomerDto customerDto = customerService.getCustomer(contractDto.getCreatedBy());
        OrganizationDto organizationDto = customerService.getOrganization(contractDto.getCreatedBy());

        // Nguoi dang thuc hien
        RecipientDto currentRecipient = getCurrentRecipient(contractDto, recipientId);
        ParticipantDto currentParticipant = getCurrentParticipant(contractDto, recipientId);

        try {

            boolean find = false;
            boolean reviewerIsProcessed = checkReviewerIsProcessed(contractDto, currentParticipant);

            // Kiem tra co doi tac dang xu ly song song // khong?
            boolean equalOrder = participantEqualOrder(contractDto, currentParticipant);
            List<RecipientDto> recipients = new ArrayList<>();
            for (var participant : contractDto.getParticipants()) {
                for (var recipient : participant.getRecipients()) {
                    recipient.setParticipant(participant);
                    recipients.add(recipient);
                }
            }

            if (equalOrder) {
                contractService.sortParallel(contractDto, recipients);
            }

            int prevOrder = -1;
            for (RecipientDto recipientDto : recipients) {
                // con nguoi xem xet cung thu tu voi nguoi xem xet trong cung to chuc chua xu ly thi dung
                if (recipientDto.getId() != recipientId
                        && recipientDto.getRole() == RecipientRole.REVIEWER.getDbVal()
                        && recipientDto.getParticipant().equals(currentParticipant)
                        && recipientDto.getOrdering() == currentRecipient.getOrdering()
                        && recipientDto.getStatus() == 1
                ) {
                    log.info("recipient-{} haven't processed yet", recipientId);
                    return null;
                }

                // TODO: check them truong hop gui thong tin nguoi xu ly cua doi tac tiep theo
                if (find && recipientDto.getId() != recipientId && recipientDto.getStatus() == 0) {
                    // la nguoi ky
                    if (recipientDto.getRole() == RecipientRole.SIGNER.getDbVal().intValue()) {
                        if (reviewerIsProcessed) {
                            log.info("[processReviewContract][contract-{}] xong qua trinh xem xet chuyen ky", contractId);
                            reviewerToSigner(contractDto, currentParticipant, customerDto, organizationDto, approveType);
                            return null;
                        }
                    } else if (recipientDto.getRole() == RecipientRole.REVIEWER.getDbVal() // chuyen den nguoi xem xet tiep cua cung to chuc
                            && recipientDto.getParticipant().getId() == currentParticipant.getId()) {

                        log.info("[processReviewContract][contract-{}] find other reviewer of participant-{} ", contractId, currentParticipant.getId());
                        if (prevOrder != -1 && prevOrder != recipientDto.getOrdering()) {
                            return null;
                        }

                        // cap nhat trang thai dang xu ly
                        recipientDto.setStatus(RecipientStatus.PROCESSING.getDbVal());
                        contractService.changeRecipientProcessing(recipientDto.getId());

                        // gui thong bao den recipientDto
                        signFlowNotifyRequest = getSignFlowNotifyRequest(contractDto,
                                recipientDto.getParticipant(), recipientDto, approveType, currentRecipient, customerDto, organizationDto);

                        // tao thong bao
                        SignFlowNotifyResponse res = notificationService.sendSignFlowNotify(signFlowNotifyRequest);
                        log.info("[processReviewContract][contract-{}] notify, {}", res, contractId);
                    }

                    prevOrder = recipientDto.getOrdering();
                } else if (recipientDto.getId() == recipientId) {
                    find = true;
                }

            } // end loop recipients


            // TODO: Kiem tra da hoan thanh ky HD
        } catch (Exception e) {

            log.error("error", e);
            error = ExceptionUtils.getFullStackTrace(e);
        } finally {
            // dong y
            if (approveType == 1) {
                checkFinish(contractDto, approveType, currentRecipient, customerDto, organizationDto);
            }
        }

        return error;
    }

    /**
     * Cap nhat trang thai cua tat ca Signer
     *
     * @param contractDto
     * @param currentParticipant
     * @param customerDto
     * @param organizationDto
     * @param approveType
     */
    private void reviewerToSigner(ContractDto contractDto, ParticipantDto currentParticipant,
                                  CustomerDto customerDto, OrganizationDto organizationDto, int approveType) {

        int minOrder = -1;

        for (var participantDto : contractDto.getParticipants()) {
            for (var recipientDto : participantDto.getRecipients()) {
                if (recipientDto.getRole() == RecipientRole.SIGNER.getDbVal()
                        && recipientDto.getOrdering() == 1
                        && recipientDto.getStatus() == 0
                ) {
                    if (minOrder == -1 || participantDto.getOrdering() < minOrder) {
                        minOrder = participantDto.getOrdering();
                    }

                    if (participantDto.getOrdering() == minOrder) {
                        noticeToRecipient(contractDto, customerDto, participantDto, recipientDto, null, organizationDto, approveType);
                    }
                }
            }
        }
    }

    private void singerToArchiver(ContractDto contractDto, CustomerDto customerDto, ParticipantDto currentParticipant,
                                  OrganizationDto organizationDto, int approveType) {

        int minOrder = -1;
        List<RecipientDto> recipients = new ArrayList<>();

        for (var participantDto : contractDto.getParticipants()) {
            if (participantDto.getOrdering() > currentParticipant.getOrdering()) {
                break;
            }

            for (var recipientDto : participantDto.getRecipients()) {
                if (recipientDto.getRole() == RecipientRole.ARCHIVER.getDbVal()
                        && recipientDto.getOrdering() == 1
                        && recipientDto.getStatus() == 0) {

                    if (minOrder == -1 || participantDto.getOrdering() < minOrder) {
                        minOrder = participantDto.getOrdering();
                    }

                    if (participantDto.getOrdering() == minOrder) {
                        recipientDto.setParticipant(participantDto);
                        recipients.add(recipientDto);
                    }
                }
            }
        }

        for (var recipientDto : recipients) {
            noticeToRecipient(contractDto, customerDto, recipientDto.getParticipant(), recipientDto, null, organizationDto, approveType);
        }

        if (recipients.size() > 0) {
            return;
        }

        log.info("khong co van thu chuyen luong xu ly sang to chuc tiep theo");
        switchToNextParticipant(contractDto, customerDto, null, currentParticipant, organizationDto, approveType);
    }

    private void coordinatorToNext(ContractDto contractDto, CustomerDto customerDto, ParticipantDto currentParticipant,
                                   OrganizationDto organizationDto, int approveType) {

        int minOrder = -1;
        var participants = contractDto.getParticipants();
        for (var participant : participants) {
            if (minOrder > -1 && participant.getOrdering() > minOrder) {
                return;
            }

            if (participant.getOrdering() > currentParticipant.getOrdering()) {
                for (var recipient : participant.getRecipients()) {
                    if (recipient.getRole() == RecipientRole.COORDINATOR.getDbVal()) {
                        noticeToRecipient(contractDto, customerDto, participant, recipient, null, organizationDto, approveType);
                        minOrder = participant.getOrdering();
                    }
                }
            }
        }

        if (minOrder > -1) {
            return;
        }

        // khong con nguoi dieu phoi nao chuyen luong xu ly ve to chuc co thu tu xu ly dau tien
        minOrder = contractDto.getParticipants().get(0).getOrdering();
        boolean findReviewr = false;
        for (var participant : participants) {
            if (participant.getOrdering() > minOrder) {
                break;
            }

            for (var recipient : participant.getRecipients()) {
                if (recipient.getRole() == RecipientRole.REVIEWER.getDbVal()
                        && recipient.getOrdering() == 1) {
                    noticeToRecipient(contractDto, customerDto, participant, recipient, null, organizationDto, approveType);
                    findReviewr = true;
                }
            }
        }

        if (findReviewr) {
            return;
        }

        // khong co nguoi xem xet chuyen den nguoi ky
        for (var participant : participants) {
            if (participant.getOrdering() > minOrder) {
                break;
            }

            for (var recipient : participant.getRecipients()) {
                if (recipient.getRole() == RecipientRole.SIGNER.getDbVal()
                        && recipient.getOrdering() == 1) {
                    noticeToRecipient(contractDto, customerDto, participant, recipient, null, organizationDto, approveType);
                }
            }
        }
    }
    private  void switchToNextParticipant(ContractDto contractDto, CustomerDto customerDto,
                                          RecipientDto currentRecipient, ParticipantDto currentParticipant,
                                          OrganizationDto organizationDto, int approveType) {
        int minOrder = -1;
        for (var participantDto : contractDto.getParticipants()) {
            if (participantDto.getOrdering() > currentParticipant.getOrdering()) {
                minOrder = participantDto.getOrdering();
                break;
            }
        }


        boolean findReviewer = false;
        for (var participantDto : contractDto.getParticipants()) {
            if (participantDto.getOrdering() == minOrder) {
                for (var recipientDto : participantDto.getRecipients()) {
                    if (recipientDto.getRole() == RecipientRole.REVIEWER.getDbVal()
                            && recipientDto.getOrdering() == 1) {

                        noticeToRecipient(contractDto, customerDto, participantDto, recipientDto, currentRecipient, organizationDto, approveType);
                        findReviewer = true;
                    }
                }
            }
        }

        if (findReviewer) {
            return;
        }
        // khong co nguoi xem xet chuyen den nguoi ky
        for (var participantDto : contractDto.getParticipants()) {
            if (participantDto.getOrdering() == minOrder) {
                for (var recipientDto : participantDto.getRecipients()) {
                    if (recipientDto.getRole() == RecipientRole.SIGNER.getDbVal()
                            && recipientDto.getOrdering() == 1) {

                        noticeToRecipient(contractDto, customerDto, participantDto, recipientDto, null, organizationDto, approveType);
                    }
                }
            }
        }
    }
    private void noticeToRecipient(ContractDto contractDto, CustomerDto customerDto,
                                   ParticipantDto participantDto,
                                   RecipientDto recipientDto, RecipientDto currRecipient,
                                   OrganizationDto organizationDto, int approveType) {
        // cap nhat trang thai dang xu ly
        recipientDto.setStatus(RecipientStatus.PROCESSING.getDbVal());
        contractService.changeRecipientProcessing(recipientDto.getId());

        // gui thong bao den recipientDto
        var signFlowNotifyRequest = getSignFlowNotifyRequest(contractDto,
                participantDto, recipientDto, approveType, currRecipient, customerDto, organizationDto);

        // tao thong bao
        var res = notificationService.sendSignFlowNotify(signFlowNotifyRequest);
        log.info("[noticeToRecipient][contract-{}] notify, {}", contractDto.getId(), res);
    }
    /**
     * Ky HD
     *
     * @param client
     * @param job
     */
    @ZeebeWorker(type = "sign-contract", name = "main-worker")
    public void signContract(final JobClient client, final ActivatedJob job) {

        logging(job);

        Map<String, Object> zeebeVariables = new HashMap<>();
        String error = null;

        try {
            // get variables
            int contractId = Integer.parseInt(job.getVariablesAsMap().get("contractId").toString());
            int actionType = Integer.parseInt(job.getVariablesAsMap().get("actionType").toString());
            int approveType = Integer.parseInt(job.getVariablesAsMap().get("approveType").toString());
            int recipientId = Integer.parseInt(job.getVariablesAsMap().get("recipientId").toString());
            int participantId = Integer.parseInt(job.getVariablesAsMap().get("participantId").toString());
            String contractJson = job.getVariablesAsMap().get("contract").toString();

            ContractDto contractDto = objectMapper.readValue(contractJson, ContractDto.class);

            // xu ly nghiep vu
            error = processSignContract(contractDto, contractId, actionType, approveType, participantId, recipientId);

            // Kiem tra nguoi ky cuoi cung
        } catch (Exception e) {
            log.error("error", e);

            error = ExceptionUtils.getFullStackTrace(e);
        } finally {
            if (error != null) {
                zeebeVariables.put("error", error);
            }
            client.newCompleteCommand(job.getKey()).variables(zeebeVariables).send().join();
        }
    }

    /**
     * Ban hanh HD
     *
     * @param client
     * @param job
     */
    @ZeebeWorker(type = "publish-contract", name = "main-worker")
    public void publishContract(final JobClient client, final ActivatedJob job) {

        logging(job);

        Map<String, Object> zeebeVariables = new HashMap<>();
        String error = null;

        try {
            // get variables
            int contractId = Integer.parseInt(job.getVariablesAsMap().get("contractId").toString());
            int actionType = Integer.parseInt(job.getVariablesAsMap().get("actionType").toString());
            int approveType = Integer.parseInt(job.getVariablesAsMap().get("approveType").toString());
            int recipientId = Integer.parseInt(job.getVariablesAsMap().get("recipientId").toString());
            int participantId = Integer.parseInt(job.getVariablesAsMap().get("participantId").toString());
            String contractJson = job.getVariablesAsMap().get("contract").toString();

            ContractDto contractDto = objectMapper.readValue(contractJson, ContractDto.class);

            // Van thu ban hanh?
            if (actionType != RecipientRole.ARCHIVER.getDbVal().intValue())
                return;

            // xu ly nghiep vu
            error = processPublishContract(contractDto, contractId, actionType, approveType, participantId, recipientId);
        } catch (Exception e) {
            log.error("error", e);

            error = ExceptionUtils.getFullStackTrace(e);
        } finally {
            if (error != null) {
                zeebeVariables.put("error", error);
            }
            client.newCompleteCommand(job.getKey()).variables(zeebeVariables).send().join();
        }
    }

    /**
     * Tim nguoi xu ly tiep theo
     *
     * @param contractId
     * @param actionType
     * @param approveType
     * @param participantId
     * @param recipientId
     * @return
     */
    private String processPublishContract(ContractDto contractDto, int contractId, int actionType, int approveType, int participantId, int recipientId) {

        String error = null;
        SignFlowNotifyRequest signFlowNotifyRequest;

        // kiem tra trang thai contract.status co khop voi actionType khong
        if (contractDto == null
                || (actionType == 0 && ContractStatus.CREATED.getDbVal().intValue() != contractDto.getStatus())        // khong phai trang thai CREATED
                || (actionType != 0 && ContractStatus.PROCESSING.getDbVal().intValue() != contractDto.getStatus())  // Khong phai trang thai PROCESSING
        ) {
            error = "Trang thai Contract khong hop le";
            return error;
        }

        // Lay thong tin customer tao HD
        var customerDto = customerService.getCustomer(contractDto.getCreatedBy());
        var organizationDto = customerService.getOrganization(contractDto.getCreatedBy());

        // Nguoi dang thuc hien
        var currentRecipient = getCurrentRecipient(contractDto, recipientId);
        var currentParticipant = getCurrentParticipant(contractDto, recipientId);

        // co van thu cung thu tu trong cung to chuc chua xu ly thi dung
        for (var recipientDto : currentParticipant.getRecipients()) {
            if (recipientDto.getId() != recipientId
                    && recipientDto.getRole() == RecipientRole.ARCHIVER.getDbVal()
                    && recipientDto.getOrdering() == currentRecipient.getOrdering()
                    && recipientDto.getStatus() == 1) {

                return null;
            }
        }

        try {

            boolean findVt = false;
            for (var recipientDto : currentParticipant.getRecipients()) {
                if (recipientDto.getRole() == currentRecipient.getRole()
                        && recipientDto.getOrdering() > currentRecipient.getOrdering()) {

                    // cap nhat trang thai dang xu ly
                    recipientDto.setStatus(RecipientStatus.PROCESSING.getDbVal());
                    contractService.changeRecipientProcessing(recipientDto.getId());

                    // gui thong bao den recipientDto
                    signFlowNotifyRequest = getSignFlowNotifyRequest(contractDto,
                            currentParticipant, recipientDto, approveType, currentRecipient, customerDto, organizationDto);

                    SignFlowNotifyResponse res = notificationService.sendSignFlowNotify(signFlowNotifyRequest);

                    log.info("notify, {}", res);
                    findVt = true;
                }
            }

            if (findVt) {
                return null;
            }

            // tim van thu o to chuc khac co cung thu tu xu ly
            for (var participant : contractDto.getParticipants()) {
                if (participant.getOrdering() > currentParticipant.getOrdering()) {
                    break;
                }

                for (var recipient : participant.getRecipients()) {
                    if (currentParticipant.getId() != participant.getId()
                            && currentParticipant.getOrdering() == participant.getOrdering()
                            && recipient.getRole() == RecipientRole.ARCHIVER.getDbVal()
                            && recipient.getProcessAt() == null
                    ) {
                        return null;
                    }
                }
            }

            // khong con van thu chuyen sang to chuc tiep
            log.info("[contract-{}] khong con van thu chuyen to chuc tiep", contractId);
            switchToNextParticipant(contractDto, customerDto, currentRecipient, currentParticipant, organizationDto, approveType);
        } catch (Exception e) {

            log.error("error", e);
            error = ExceptionUtils.getFullStackTrace(e);
        } finally {
            // dong y
            if (approveType == 1) {
                checkFinish(contractDto, approveType, currentRecipient, customerDto, organizationDto);
            }
        }

        return error;
    }

    private String processCoordinatorContract(ContractDto contractDto, int contractId, int actionType, int approveType, int participantId, int recipientId) {

        String error = null;

        // kiem tra trang thai contract.status co khop voi actionType khong
        if (contractDto == null
                || (actionType == 0 && ContractStatus.CREATED.getDbVal().intValue() != contractDto.getStatus())        // khong phai trang thai CREATED
                || (actionType != 0 && ContractStatus.PROCESSING.getDbVal().intValue() != contractDto.getStatus())  // Khong phai trang thai PROCESSING
        ) {
            error = "Trang thai Contract khong hop le";
            return error;
        }

        // Lay thong tin customer tao HD
        var customerDto = customerService.getCustomer(contractDto.getCreatedBy());
        var organizationDto = customerService.getOrganization(contractDto.getCreatedBy());

        // Nguoi dang thuc hien
        var currentRecipient = getCurrentRecipient(contractDto, recipientId);
        var currentParticipant = getCurrentParticipant(contractDto, recipientId);

        try {
            coordinatorToNext(contractDto, customerDto, currentParticipant, organizationDto, approveType);
        } catch (Exception e) {

            log.error("error", e);
            error = ExceptionUtils.getFullStackTrace(e);
        } finally {
            // dong y
            if (approveType == 1) {
                checkFinish(contractDto, approveType, currentRecipient, customerDto, organizationDto);
            }
        }

        return error;
    }

    /**
     * Xu ly luong ky HD
     *
     * @param contractDto
     * @param contractId
     * @param actionType
     * @param approveType
     * @param participantId
     * @param recipientId
     * @return
     */
    private String processSignContract(ContractDto contractDto, int contractId, int actionType, int approveType, int participantId, int recipientId) {

        String error = null;
        SignFlowNotifyRequest signFlowNotifyRequest;

        // kiem tra trang thai contract.status co khop voi actionType khong
        if (contractDto == null
                || (actionType == 0 && ContractStatus.CREATED.getDbVal().intValue() != contractDto.getStatus())        // khong phai trang thai CREATED
                || (actionType != 0 && ContractStatus.PROCESSING.getDbVal().intValue() != contractDto.getStatus())  // Khong phai trang thai PROCESSING
        ) {
            error = "Trang thai Contract khong hop le";
            return error;
        }

        // Lay thong tin customer tao HD
        var customerDto = customerService.getCustomer(contractDto.getCreatedBy());
        var organizationDto = customerService.getOrganization(contractDto.getCreatedBy());

        // Nguoi dang thuc hien
        var currentRecipient = getCurrentRecipient(contractDto, recipientId);
        var currentParticipant = getCurrentParticipant(contractDto, recipientId);

        // co nguoi ky cung thu tu trong cung to chuc chua xu ly thi dung
        for (var recipientDto : currentParticipant.getRecipients()) {
            if (recipientDto.getId() != recipientId
                    && recipientDto.getRole() == RecipientRole.SIGNER.getDbVal()
                    && recipientDto.getOrdering() == currentRecipient.getOrdering()
                    && recipientDto.getStatus() == 1) {

                return null;
            }
        }

        // kiem tra signer da xu ly het chua
        boolean signerIsProcessed = checkSignerIsProcessed(contractDto, currentParticipant);

        try {
            if (signerIsProcessed) {
                log.info("[processSignContract][contract-{}] chuyen van thu", contractDto.getId());
                singerToArchiver(contractDto, customerDto, currentParticipant, organizationDto, approveType);
            } else {
                log.info("[processSignContract][contract-{}] find other signer", contractId);
                // Tìm người ký còn lại của tổ chức
                int prevOrder = -1;
                for (var recipientDto : currentParticipant.getRecipients()) {
                    if (recipientDto.getRole() == RecipientRole.SIGNER.getDbVal().intValue()
                            && recipientDto.getStatus() == 0) {

                        if (prevOrder != -1 && prevOrder != recipientDto.getOrdering()) {
                            break;
                        }
                        // cap nhat trang thai dang xu ly
                        recipientDto.setStatus(RecipientStatus.PROCESSING.getDbVal());
                        contractService.changeRecipientProcessing(recipientDto.getId());

                        // gui thong bao den recipientDto
                        signFlowNotifyRequest = getSignFlowNotifyRequest(contractDto,
                                currentParticipant, recipientDto, approveType, currentRecipient, customerDto, organizationDto);
                        // tao thong bao
                        var res = notificationService.sendSignFlowNotify(signFlowNotifyRequest);

                        log.info("[processSignContract][contract-{}] notify, {}", contractId, res);
                        prevOrder = recipientDto.getOrdering();
                    }
                }
            }
            return null;
        } catch (Exception e) {

            log.error("error", e);
            error = ExceptionUtils.getFullStackTrace(e);
        } finally {
            // dong y
            if (approveType == 1) {
                checkFinish(contractDto, approveType, currentRecipient, customerDto, organizationDto);
            }
        }

        return error;
    }

    /**
     * Kiem tra hoan thanh luong ky HD
     *
     * @param contractDto
     * @param approveType
     * @param currentRecipient
     * @param customerDto
     * @param organizationDto
     */
    private void checkFinish(ContractDto contractDto, int approveType,
                             RecipientDto currentRecipient, CustomerDto customerDto, OrganizationDto organizationDto) {

        boolean finish = true;

        try {
            // Kiem tra hoan thanh luong ky HD - Co the su dung get last item cua recipient
            for (ParticipantDto participantDto : contractDto.getParticipants()) {
                for (RecipientDto recipientDto : participantDto.getRecipients()) {
                    if (recipientDto.getStatus() != 2) {
                        finish = false;
                        return;
                    }
                }
            }
        } catch (Exception e) {
            finish = false;
            log.error("error", e);
        } finally {

            // Da hoan thanh luong HD
            if (finish) {
                try {
                    // 1. Cap nhat thong tin HD la hoan thanh ContractStatus.SIGNED
                    ContractDto res = contractService.changeStatus(contractDto.getId(), ContractStatus.SIGNED.getDbVal());
                    log.info("Finish contract: " + res);

                    if (contractDto.getCeCAPush() == 1) {
                        log.info("start send file to CeCA");
                        var message = contractService.sendContractToCeCA(contractDto.getId());
                        if (message.isSuccess()) {
                            log.info("send file successfully!");
                        }
                    }

                    SignFlowNotifyRequest signFlowNotifyRequest;
                    SignFlowNotifyResponse resFinish;

                    // Gui thong bao den nguoi tao HD
                    try {
                        signFlowNotifyRequest = getSignFlowNotifyCustomerRequest(contractDto,
                                3, currentRecipient, customerDto, organizationDto);
                        resFinish = notificationService.sendSignFlowNotify(signFlowNotifyRequest);
                        log.info("notify, {}", resFinish);
                    } catch (Exception e) {
                        log.error("error", e);
                    }

                    // 2. Gui thong bao den nguoi tham gia
                    for (ParticipantDto participantDto : contractDto.getParticipants()) {

                        for (RecipientDto recipientDto : participantDto.getRecipients()) {

                            try {
                                // approveType = 3 - Hoan thanh HD
                                signFlowNotifyRequest = getSignFlowNotifyRequest(contractDto,
                                        participantDto, recipientDto, 3, currentRecipient, customerDto, organizationDto);

                                // tao thong bao
                                resFinish = notificationService.sendSignFlowNotify(signFlowNotifyRequest);

                                log.info("notify, {}", resFinish);
                            } catch (Exception e) {
                                log.error("error", e);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("error", e);
                }
            }
        }
    }
}
