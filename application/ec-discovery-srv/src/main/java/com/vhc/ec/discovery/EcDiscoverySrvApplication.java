package com.vhc.ec.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EcDiscoverySrvApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcDiscoverySrvApplication.class, args);
    }

}
