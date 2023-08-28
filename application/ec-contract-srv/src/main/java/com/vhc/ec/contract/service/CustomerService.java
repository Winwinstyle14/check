package com.vhc.ec.contract.service;

import com.vhc.ec.contract.dto.CustomerDto;
import com.vhc.ec.contract.dto.FindCustomerByEmailRequest;
import com.vhc.ec.contract.dto.OrganizationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Lấy thông tin của khách hàng thông qua ec-customer-srv
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    private final RestTemplate restTemplate;
    @Value("${vhc.ec.micro-services.customer.api-url}")
    private String customerApi;

    /**
     * Thông tin chi tiết của tổ chức theo mã khách hàng
     *
     * @param customerId Mã số tham chiếu tới khách hàng
     * @return {@link OrganizationDto} Thông tin chi tiết của tổ chức
     */
    public Optional<OrganizationDto> getOrganizationByCustomer(int customerId) {
        return getOrganization(
                this.customerApi + "/internal/organizations/by-customer/" + customerId
        );
    }

    /**
     * Lấy thông tin chi tiết của tổ chức theo mã tổ chức
     *
     * @param id Mã tổ chức
     * @return Thông tin chi tiết của tổ chức
     */
    public Optional<OrganizationDto> getOrganizationById(int id) {
        return getOrganization(
                this.customerApi + "/internal/organizations/" + id
        );
    }

    /**
     * Tìm kiếm khách hàng theo địa chỉ email
     *
     * @param email
     * @return
     */
    public boolean findCustomerByEmail(String email) {

        try {
            var request = FindCustomerByEmailRequest
                    .builder().email(email)
                    .build();
            var response = restTemplate.postForEntity(
                    this.customerApi + "/internal/customers/getByEmail",
                    request,
                    CustomerDto.class
            );

            if (response != null && response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
                return response.getBody().getId() > 0;
            }
        } catch (Exception e) {
            log.error("error, email={}", email, e);
        }

        return false;
    }

    /**
     * Get customer by email
     *
     * @param email
     * @return
     */
    public CustomerDto getCustomerByEmail(String email) {

        try {
            var request = FindCustomerByEmailRequest
                    .builder().email(email)
                    .build();
            var response = restTemplate.postForEntity(
                    this.customerApi + "/internal/customers/getByEmail",
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

    /**
     * Get Customer by customerId
     *
     * @param id
     * @return
     */
    public CustomerDto getCustomerById(int id) {

        try {
            var response = restTemplate.getForEntity(
                    this.customerApi + "/internal/customers/" + id,
                    CustomerDto.class
            );

            if (response != null && response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("error, id={}", id, e);
        }

        return null;
    }

    /**
     * Lấy thông tin chi tiết của tổ chức
     *
     * @param apiUrl Đường dẫn lấy thông tin của tổ chức
     * @return Thông tin chi tiết của tổ chức
     */
    private Optional<OrganizationDto> getOrganization(String apiUrl) {
        final var response = restTemplate.getForEntity(
                apiUrl,
                OrganizationDto.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return Optional.ofNullable(response.getBody());
        }
        return Optional.empty();
    }

    public List<Integer> getDescendantId(int orgId) {
        var response = restTemplate.getForEntity(
                String.format("%s/internal/organizations/%d/get-descendant-id", customerApi, orgId),
                Integer[].class
        );

        return Arrays.asList(response.getBody());
    }

    public List<Integer> getAllOrgInTree(int orgId) {
        var response =  restTemplate.getForEntity(
                String.format("%s/internal/organizations/%d/get-all-org-in-tree", customerApi, orgId),
                Integer[].class
        );

        return Arrays.asList(response.getBody());
    }

    public void decreaseNumberOfContracts(int orgId) {
        log.info("decreaseNumberOfContracts {}", orgId);
        restTemplate.exchange(
                String.format("%s/internal/organizations/%d/decrease-number-of-contracts", customerApi, orgId),
                HttpMethod.PUT,
                null,
                Void.class
        );
    }
}
