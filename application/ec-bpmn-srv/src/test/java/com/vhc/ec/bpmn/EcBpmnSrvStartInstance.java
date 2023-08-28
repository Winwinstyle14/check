package com.vhc.ec.bpmn;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

import java.util.Map;

public class EcBpmnSrvStartInstance {

    public static void main(String[] args) {
        ZeebeClient client = ZeebeClient.newClientBuilder()
                                        .usePlaintext()
                                        .gatewayAddress("14.160.91.174:1326")
                                        .build();

        final ProcessInstanceEvent event = client.newCreateInstanceCommand()
                .bpmnProcessId("SignFlow")
                .latestVersion()
                .variables(Map.of("contractId", 123, "actionType", 1, "recipientId", 456))
                .send()
                .join();

        System.out.println(String.format("Started instance for processDefinitionKey='%s', bpmnProcessId='%s', version='%s' with processInstanceKey='%s'",
                event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey()));
    }
}
