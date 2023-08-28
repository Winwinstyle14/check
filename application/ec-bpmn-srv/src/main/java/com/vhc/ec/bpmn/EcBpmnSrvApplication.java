package com.vhc.ec.bpmn;

import com.vhc.ec.api.auth.EnableCustomerAuth;
import com.vhc.ec.api.versioning.EnableApiVersioning;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
@EnableApiVersioning
@EnableCustomerAuth
public class EcBpmnSrvApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcBpmnSrvApplication.class, args);
    }

}
