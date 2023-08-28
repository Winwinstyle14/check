package com.vhc.ec.contract.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Cấu hình rest template, cho phép truy vấn dữ liệu tới hệ thống nội bộ
 * thông quan name server
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
