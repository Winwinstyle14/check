package com.vhc.ec.bpmn;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;

import java.time.Duration;

public class EcBpmnSrvWorker {

    public static void main(String[] args) {

        ZeebeClient client = ZeebeClient.newClientBuilder()
                .usePlaintext()
                .gatewayAddress("14.160.91.174:1326")
                .build();

        final JobWorker paymentWorkerRegistration =
                client
                    .newWorker()
                    .jobType("payment-service")
                    .handler(new PaymentJobHandler())
                    .timeout(Duration.ofSeconds(10))
                    .open();

        final JobWorker inventoryWorkerRegistration =
                client
                        .newWorker()
                        .jobType("inventory-service")
                        .handler(new InventoryJobHandler())
                        .timeout(Duration.ofSeconds(10))
                        .open();

        final JobWorker shipmentWorkerRegistration =
                client
                        .newWorker()
                        .jobType("shipment-service")
                        .handler(new ShipmentJobHandler())
                        .timeout(Duration.ofSeconds(10))
                        .open();
    }

    private static class PaymentJobHandler implements JobHandler {
        @Override
        public void handle(final JobClient client, final ActivatedJob job) {
            // here: business logic that is executed with every job
            System.out.println("payment: " + job);
            client.newCompleteCommand(
                job.getKey()).send().whenComplete((result, exception) -> {
                    if (exception == null) {
                        System.out.println("Completed job successful with result:" + result);
                    } else {
                        System.out.println(String.format("Failed to complete job: + %s" + exception.toString()));
                    }
                }
            );
        }
    }

    private static class InventoryJobHandler implements JobHandler {
        @Override
        public void handle(final JobClient client, final ActivatedJob job) {
            // here: business logic that is executed with every job
            System.out.println("inventory: " + job);
            client.newCompleteCommand(job.getKey()).send().join();
        }
    }

    private static class ShipmentJobHandler implements JobHandler {
        @Override
        public void handle(final JobClient client, final ActivatedJob job) {
            // here: business logic that is executed with every job
            System.out.println("shipment: " + job);
            client.newCompleteCommand(job.getKey()).send().join();
        }
    }
}

