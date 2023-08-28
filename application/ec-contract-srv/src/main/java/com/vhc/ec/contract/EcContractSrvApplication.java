package com.vhc.ec.contract;

import com.vhc.ec.api.auth.EnableCustomerAuth;
import com.vhc.ec.api.versioning.EnableApiVersioning;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
@EnableEurekaClient
@EnableApiVersioning
@EnableCustomerAuth
@EnableScheduling
public class EcContractSrvApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcContractSrvApplication.class, args);
    }

    @Bean
    public TaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        return scheduler;
    }
}
