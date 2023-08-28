package com.vhc.ec.api.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Optional;

/**
 * Dịch vụ thực hiện xác thực thông tin của người dùng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final CustomerAuthProperties customerAuthProperties;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Lấy thông tin của khách hàng từ dịch vụ xác thực khách hàng
     *
     * @param jwt Mã xác thực của người dùng
     * @return Thông tin của khách hàng trả về
     */
    public Optional<TokenResponse> getCustomer(String jwt) {
        final var params = new HashMap<String, String>();
        params.put("access_token", jwt);

        final var request = new HttpEntity<>(params);
        final var response = restTemplate.postForEntity(
                customerAuthProperties.getTokenUrl(),
                request,
                TokenResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return Optional.of(
                    response.getBody()
            );
        }

        return Optional.empty();
    }

}
