package com.vhc.ec.bpmn.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhc.ec.bpmn.dto.ContractDto;
import com.vhc.ec.bpmn.service.ContractService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableZeebeClient
public class ValidateContractWorker extends BaseWorker {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    final RestTemplate restTemplate;
    final ContractService contractService;

    @ZeebeWorker(type = "validate-contract", name = "main-worker")
    public void infoService(final JobClient client, final ActivatedJob job) {

        Map<String, Object> zeebeVariables = new HashMap<>();
        boolean validInput = false;
        String error = null;
        ContractDto contractDto = null;

        try {

            logging(job);
            int contractId = Integer.parseInt(job.getVariablesAsMap().get("contractId").toString());
            int actionType = Integer.parseInt(job.getVariablesAsMap().get("actionType").toString());
            int approveType = Integer.parseInt(job.getVariablesAsMap().get("approveType").toString());

            contractDto = contractService.getContract(contractId);

            log.info("contract.get: {}", contractDto);

            if (contractDto == null) {
                error = "Contract is not exist";
                return;
            }

            // kiem tra participant
            if (contractDto.getParticipants().size() == 0 || contractDto.getParticipants().isEmpty()) {
                error = "Participant is missing";
                log.error("Participant is missing");
                return;
            }

            validInput = true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error", e);
            error = ExceptionUtils.getFullStackTrace(e);
        } finally {
            zeebeVariables.put("validInput", validInput);
            if (error != null) {
                zeebeVariables.put("error", error);
            }

            // add contract
            try {
                zeebeVariables.put("contract", objectMapper.writeValueAsString(contractDto));
            } catch (Exception e) {
                log.error("error", e);
            }

            client.newCompleteCommand(job.getKey()).variables(zeebeVariables).send().join();
        }
    }
}
