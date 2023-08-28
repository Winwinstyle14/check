package com.vhc.ec.auth.service;

import com.vhc.ec.auth.dto.AdminLoginRequest;
import com.vhc.ec.auth.dto.AdminLoginResponse;
import com.vhc.ec.auth.dto.CustomerDto;
import com.vhc.ec.auth.dto.UserViewDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * @author: VHC JSC
 * @version: 1.0
 * @since: 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {
    private final RestTemplate restTemplate;

    @Value("${vhc.ec.micro-services.api-url.admin}")
    private String apiUrl;

    public Optional<AdminLoginResponse> login(AdminLoginRequest loginRequest) {
        final var request = new HttpEntity<>(loginRequest);
        //
        final var response = restTemplate.postForEntity(
                String.format("%s/admin/user/auth", apiUrl),
                request,
                AdminLoginResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return Optional.ofNullable(response.getBody());
        }

        return Optional.empty();
    }

    public Optional<UserViewDto> getUserById(long id) {
        final var url = String.format("%s/admin/user/internal/%d", apiUrl, id);
        final var response = restTemplate.getForEntity(url, UserViewDto.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return Optional.ofNullable(response.getBody());
        }
        return Optional.empty();
    }
}
