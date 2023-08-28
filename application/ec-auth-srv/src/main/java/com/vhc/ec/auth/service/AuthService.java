package com.vhc.ec.auth.service;

import com.vhc.ec.auth.dto.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private final ContractService contractService;
    private final CustomerService customerService;
    private final CustomerTokenService tokenService;
    private final AdminUserService userService;

    /**
     * Đăng nhập tới hệ thống bằng tài khoản của khách hàng
     *
     * @param loginRequest Thông tin đăng nhập của khách hàng
     * @return {@link TokenResponse} Thông tin token được sinh ra cho khách hàng
     */
    public Optional<TokenResponse> signIn(LoginRequest loginRequest) {
        String accessToken;

        Map<String, Object> claims = new HashMap<>();
        CustomerTokenService.ClaimSubject claimSubject = null;

        // token response
        TokenResponse tokenResponse = null;

        // Xác định thông tin của người dùng
        switch (loginRequest.getType()) {
            case 0:
                final var customerLoginResponseOptional = customerService
                        .login(loginRequest);

                if (customerLoginResponseOptional.isPresent()
                        && customerLoginResponseOptional.get().isSuccess()
                        && customerLoginResponseOptional.get().getCode().equals("00")) {
                    final var customer = customerLoginResponseOptional
                            .get().getCustomer();

                    if (customer.getStatus() == 1) {
                        claimSubject = CustomerTokenService.ClaimSubject.CUSTOMER;

                        claims.put("id", customer.getId());
                        claims.put("name", customer.getName());
                        claims.put("email", customer.getEmail());
                        claims.put("phone", customer.getPhone());
                        claims.put("organizationId", customer.getOrganizationId());
                        claims.put("typeId", customer.getTypeId());
                        claims.put("organizationChange", customer.getOrganizationChange());
                    }
                } else {
                    tokenResponse = TokenResponse.builder()
                            .code(customerLoginResponseOptional.get().getCode())
                            .type("Bearer")
                            .accessToken(null)
                            .customer(null)
                            .build();
                }
                break;
            case 1:
                final var recipientLoginResponseOptional = contractService.login(loginRequest);
                if (recipientLoginResponseOptional.isPresent() && recipientLoginResponseOptional.get().isSuccess()) {
                    final var recipient = recipientLoginResponseOptional.get().getRecipient();

                    log.error("RECIPIENT = {}", recipient.toString());

                    claimSubject = CustomerTokenService.ClaimSubject.GUEST;

                    claims.put("id", recipient.getId());
                    claims.put("name", recipient.getName());
                    claims.put("email", recipient.getEmail());
                    claims.put("phone", null);
                    claims.put("organizationId", 0);
                    claims.put("typeId", 0);
                    claims.put("organizationChange", 0);
                } else {
                    tokenResponse = TokenResponse.builder()
                            .code("03")
                            .type("Bearer")
                            .accessToken(null)
                            .customer(null)
                            .build();
                }

                break;
            default:
                return Optional.empty();
        }

        if (claimSubject != null) {
            // Khởi tạo token cho khách hàng
            accessToken = tokenService.generateToken(claimSubject, claims);

            if (StringUtils.hasText(accessToken)) {
                return Optional.of(
                        TokenResponse.builder()
                                .accessToken(accessToken)
                                .code("00")
                                .type("Bearer")
                                .customer(TokenResponse.Customer.builder()
                                        .info(claims)
                                        .type(claimSubject.ordinal())
                                        .build())
                                .build()
                );
            }
        }

        return Optional.ofNullable(tokenResponse);
    }

    public Optional<AdminTokenResponse> adminLogin(AdminLoginRequest adminLoginRequest) {
        String token;

        Map<String, Object> claims = new HashMap<>();
        CustomerTokenService.ClaimSubject claimSubject = null;

        // token response
        AdminTokenResponse tokenResponse = null;

        final var adminLoginResOptional = userService
                .login(adminLoginRequest);

        if (adminLoginResOptional.isEmpty()) {
            return Optional.empty();
        }

        if (adminLoginResOptional.get().isSuccess()
                && adminLoginResOptional.get().getCode().equals("00")) {

            final var user = adminLoginResOptional
                    .get().getUser();
            claimSubject = CustomerTokenService.ClaimSubject.ADMIN;
            claims.put("id", user.getId());
            claims.put("name", user.getName());
            claims.put("email", user.getEmail());
            claims.put("phone", user.getPhone());

        } else {
            tokenResponse = AdminTokenResponse.builder()
                    .code(adminLoginResOptional.get().getCode())
                    .build();
        }

        if (claimSubject != null) {
            // Khởi tạo token cho khách hàng
            token = tokenService.generateToken(claimSubject, claims);

            if (StringUtils.hasText(token)) {
                return Optional.of(
                        AdminTokenResponse.builder()
                                .token(token)
                                .code("00")
                                .user(adminLoginResOptional.get().getUser())
                                .build()
                );
            }
        }

        return Optional.ofNullable(tokenResponse);
    }

}
