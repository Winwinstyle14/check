package com.vhc.ec.auth.service;

import com.vhc.ec.auth.dto.LoginRequest;
import com.vhc.ec.auth.dto.RecipientDto;
import com.vhc.ec.auth.dto.RecipientLoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    private final RestTemplate restTemplate;
    @Value("${vhc.ec.micro-services.api-url.contract}")
    private String apiUrl;

    /**
     * Kiểm tra thông tin đăng nhập từ dịch vụ quản lý hợp đồng
     *
     * @param loginRequest Thông tin đăng nhập của khách hàng
     * @return {@link RecipientLoginResponse}
     */
    public Optional<RecipientLoginResponse> login(LoginRequest loginRequest) {
        final var request = new HttpEntity<>(loginRequest);

        final var response = restTemplate.postForEntity(
                String.format("%s/auth", apiUrl),
                request,
                RecipientLoginResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            final var loginResponse = response.getBody();

            return Optional.ofNullable(loginResponse);
        }

        return Optional.empty();
    }

    public Optional<RecipientDto> getRecipientById(int id) {
        final var response = restTemplate.getForEntity(
                String.format(String.format("%s/recipients/internal/%d", apiUrl, id)),
                RecipientDto.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            final var recipient = response.getBody();
            return Optional.of(recipient);
        }

        return Optional.empty();
    }
}
