package com.vhc.ec.notification.service;

import com.vhc.ec.notification.dto.CustomerDto;
import com.vhc.ec.notification.dto.FindCustomerByEmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    private final RestTemplate restTemplate;
    @Value("${vhc.ec.micro-services.customer.api-url}")
    private String customerApi;

    public void decreaseNumberOfSms(int orgId) {
        try {
            restTemplate.exchange(
                    String.format("%s/internal/organizations/%d/decrease-number-of-sms", customerApi, orgId),
                    HttpMethod.PUT,
                    null,
                    Void.class
            );
        } catch (Exception ex) {
            log.error("error: {}", ex);
        }

    }

    public CustomerDto getCustomerByEmail(String email) {

        try {
            var request = FindCustomerByEmailRequest
                    .builder().email(email)
                    .build();
            var response = restTemplate.postForEntity(
                    customerApi + "/internal/customers/getByEmail",
                    request,
                    CustomerDto.class
            );

            if (response != null && response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("error, email={}", email, e);
        }

        return null;
    }
}
