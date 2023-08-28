package com.vhc.ec.api.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties(CustomerAuthProperties.class)
public class CustomerAutoConfiguration {

    @Bean
    public TokenService tokenService(CustomerAuthProperties customerAuthProperties) {
        return new TokenService(customerAuthProperties);
    }

    @Bean
    public JwtTokenFilter jwtTokenFilter(TokenService tokenService) {
        return new JwtTokenFilter(tokenService);
    }

    @Bean
    @ConditionalOnMissingBean
    public CustomerWebSecurity customerWebSecurity(CustomerAuthProperties customerAuthProperties, TokenService tokenService, JwtTokenFilter jwtTokenFilter) {
        return new CustomerWebSecurity(customerAuthProperties, jwtTokenFilter);
    }

    @Bean
    @LoadBalanced
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
