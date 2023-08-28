package com.vhc.ec.auth;

import com.vhc.ec.api.auth.EnableCustomerAuth;
import com.vhc.ec.api.versioning.EnableApiVersioning;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
@EnableEurekaClient
@EnableApiVersioning
@EnableCustomerAuth
public class EcAuthSrvApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcAuthSrvApplication.class, args);
    }

}

@Configuration
class RestTemplateConfig {

    // Create a bean for restTemplate to call services
    @Bean
    @LoadBalanced        // Load balance between service instances running at different ports.
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
