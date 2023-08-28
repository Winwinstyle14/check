package com.vhc.ec.bpmn.controller;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.bpmn.config.BpmnConfig;
import com.vhc.ec.bpmn.dto.StartBpmnInstanceRequest;
import com.vhc.ec.bpmn.dto.StartBpmnInstanceResponse;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.Map;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@RequiredArgsConstructor
@Slf4j
public class BpmnController {

    final ZeebeClient zeebeClient;
    final BpmnConfig bpmnConfig;

    /**
     * Start Bpmn Sign_Flow Instance
     *
     * @param request
     * @return
     */
    @PostMapping("/internal/bpmn/startSignFlowInstance")
    public StartBpmnInstanceResponse create(@Valid @RequestBody StartBpmnInstanceRequest request) {

        boolean status = false;
        String message = "";
        try {
            final ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId(bpmnConfig.getSignFlow())
                    .latestVersion()
                    .variables(Map.of(
                            "contractId", request.getContractId(),
                            "actionType", request.getActionType(),
                            "approveType", request.getApproveType(),
                            "recipientId", request.getRecipientId(),
                            "participantId", request.getParticipantId())
                    )
                    .send()
                    .join();

            status = true;
            message = String.format("start Sign_Flow \"%s\" success", request);
            log.info(message);
        } catch (Exception e) {
            log.error("start Sign_Flow: {}", request, e);

            message = String.format("start Sign_Flow \"%s\" false", request);
        }

        return StartBpmnInstanceResponse.builder()
                .success(status)
                .message(message)
                .build();
    }
}
