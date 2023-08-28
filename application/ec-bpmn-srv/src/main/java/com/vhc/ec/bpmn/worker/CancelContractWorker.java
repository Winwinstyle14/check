package com.vhc.ec.bpmn.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableZeebeClient
public class CancelContractWorker extends BaseWorker {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    final RestTemplate restTemplate;
    final ContractService contractService;
    final CustomerService customerService;
    final NotificationService notificationService;

    /**
     * Huy HD
     *
     * @param client
     * @param job
     */
    @ZeebeWorker(type = "cancel-contract", name = "main-worker")
    public void cancelContract(final JobClient client, final ActivatedJob job) {

        logging(job);

        Map<String, Object> zeebeVariables = new HashMap<>();
        boolean validInput = false;
        String error = null;
        ContractDto contractDto = null;

        try {
            int approveType = Integer.parseInt(job.getVariablesAsMap().get("approveType").toString());
            String notificationCode = job.getVariablesAsMap().get("notificationCode").toString();

            String contractJson = job.getVariablesAsMap().get("contract").toString();

            contractDto = objectMapper.readValue(contractJson, ContractDto.class);

            log.info("contract.get: {}", contractDto);

            // Lay thong tin customer tao HD
            CustomerDto customerDto = customerService.getCustomer(contractDto.getCreatedBy());
            OrganizationDto organizationDto = customerService.getOrganization(contractDto.getCreatedBy());

            SignFlowNotifyRequest request;

            for (ParticipantDto participantDto : contractDto.getParticipants()) {

                for (RecipientDto recipientDto : participantDto.getRecipients()) {

                    if (recipientDto.getStatus() != 0) {
                        // gui thong bao tu choi den recipientDto
                        try {
                            request = new SignFlowNotifyRequest();

                            if ("phone".equals(recipientDto.getLoginBy())) {
                                notificationCode = "sms_sign_flow_cancel";
                            } else {
                                notificationCode = "sign_flow_cancel";
                            }

                            request.setNotificationCode(notificationCode);
                            request.setApproveType(approveType);

                            request.setContractId(contractDto.getId());
                            request.setContractName(contractDto.getName());
                            request.setContractUrl("" + contractDto.getId());
                            request.setContractNotes(contractDto.getNotes());

                            request.setRecipientName(recipientDto.getName());
                            request.setRecipientEmail(recipientDto.getEmail());
                            request.setRecipientPhone(recipientDto.getPhone());

                            request.setSenderName(customerDto.getName());
                            request.setSenderParticipant(organizationDto.getName());

                            request.setReasonReject(contractDto.getReasonReject());
                            request.setRecipientReject(customerDto.getName());
                            request.setLoginBy(recipientDto.getLoginBy());
                            request.setOrgId(organizationDto.getId());
                            request.setBrandName(organizationDto.getBrandName());
                            request.setSmsUser(organizationDto.getSmsUser());
                            request.setSmsPass(organizationDto.getSmsPass());
                            request.setSmsSendMethor(organizationDto.getSmsSendMethor());
                            request.setContractUid(contractDto.getContractUid());

                            request.setAccessCode(recipientDto.getPassword());

                            if (recipientDto.getPassword() != null && !recipientDto.getPassword().equals("")) {
                                request.setLoginType("1");
                            }

                            SignFlowNotifyResponse res = notificationService.sendSignFlowNotify(request);

                            log.info("notify, {}", res);
                        } catch (Exception e) {
                            log.error("error", e);
                            error = e.getMessage();
                        }
                    }
                }
            }

//            SignFlowNotifyRequest request = new SignFlowNotifyRequest();
//            request.setNotificationCode(notificationCode);
//
//            //request.setActionType(recipientDto.getRole());
//            request.setApproveType(approveType);
//
//            request.setContractId(contractDto.getId());
//            request.setContractName(contractDto.getName());
//            request.setContractUrl("" + contractDto.getId());
//            request.setContractNotes(contractDto.getNotes());
//
//            request.setRecipientName(customerDto.getName());
//            request.setRecipientEmail(customerDto.getEmail());
//            request.setRecipientPhone(customerDto.getPhone());
//
//            request.setSenderName(customerDto.getName());
//            request.setSenderParticipant(organizationDto.getName());
//
//            SignFlowNotifyResponse res = notificationService.sendSignFlowNotify(request);
//
//            log.info("notify, {}", res);
        } catch (Exception e) {
            log.error("error", e);
            error = e.getMessage();
        } finally {
            if (error != null) {
                zeebeVariables.put("error", error);
            }

            client.newCompleteCommand(job.getKey()).variables(zeebeVariables).send().join();
        }
    }
}
