package com.vhc.ec.auth.service;

import com.vhc.ec.auth.dto.CustomerDto;
import com.vhc.ec.auth.dto.CustomerLoginResponse;
import com.vhc.ec.auth.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    private final RestTemplate restTemplate;

    @Value("${vhc.ec.micro-services.api-url.customer}")
    private String apiUrl;

    /**
     * Lấy thông tin đăng nhập của khách hàng thành viên
     *
     * @param loginRequest Thông tin đăng nhập của khách hàng thành viên
     * @return {@link Optional<CustomerLoginResponse>}
     */
    public Optional<CustomerLoginResponse> login(LoginRequest loginRequest) {
        final var request = new HttpEntity<>(loginRequest);
        //
        final var response = restTemplate.postForEntity(
                String.format("%s/auth", apiUrl),
                request,
                CustomerLoginResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return Optional.ofNullable(response.getBody());
        }

        return Optional.empty();
    }

    /**
     * Lấy thông tin của khách hàng trên hệ thống,
     * thông qua mã số tham chiếu của khách hàng.
     *
     * @param id customer id
     * @return {@link Optional<CustomerDto>}
     */
    public Optional<CustomerDto> getCustomerById(int id) {
        final var url = String.format("%s/internal/customers/%d", apiUrl, id);
        final var response = restTemplate.getForEntity(url, CustomerDto.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Optional.ofNullable(response.getBody());
        }
        return Optional.empty();
    }

    /**
     * Lấy thông tin của khách hàng thành viên trên hệ thống,
     * theo địa chỉ email của khách hàng
     *
     * @param email Địa chỉ email của khách hàng
     * @return {@link CustomerDto}
     */
    public Optional<CustomerDto> getCustomerByEmail(String email) {
        final var map = new HashMap<>();
        map.put("email", email);

        final var request = new HttpEntity<>(map);

        final var response = restTemplate.postForEntity(
                apiUrl,
                request,
                CustomerDto.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return Optional.ofNullable(response.getBody());
        }

        return Optional.empty();
    }
}
