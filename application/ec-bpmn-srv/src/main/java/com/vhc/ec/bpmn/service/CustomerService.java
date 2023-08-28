package com.vhc.ec.bpmn.service;

import com.vhc.ec.bpmn.dto.CustomerDto;
import com.vhc.ec.bpmn.dto.OrganizationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    @Value("${vhc.micro-services.customer.api-url}")
    private  String customerUrl;

    final RestTemplate restTemplate;

    public CustomerDto getCustomer(int customerId) {

        return restTemplate.getForEntity(customerUrl + "/internal/customers/" + customerId,
                CustomerDto.class).getBody();
    }

    public OrganizationDto getOrganization(int customerId) {

        return restTemplate.getForEntity(customerUrl + "/internal/organizations/by-customer/" + customerId,
                OrganizationDto.class).getBody();
    }
}
