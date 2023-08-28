package com.vhc.ec.auth.controller;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.auth.dto.*;
import com.vhc.ec.auth.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/auth")
@ResponseStatus(HttpStatus.OK)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;


    /**
     * Đăng nhập tới hệ thống, sử dụng tài khoản khách hàng
     *
     * @param loginRequestDto Thông tin đăng nhập của khách hàng
     * @return Thông tin xác thực của người dùng sau khi đăng nhập
     */
    @PostMapping
    public ResponseEntity<TokenResponse> signIn(@RequestBody @Valid LoginRequest loginRequestDto) {
        final var tokenResponseOptional = authService.signIn(loginRequestDto);

        return tokenResponseOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().body(
                        TokenResponse.builder()
                                .type("Bearer")
                                .accessToken(null)
                                .customer(null)
                                .build()
                ));
    }

    @PostMapping("/reset-password")
    public HttpEntity<ResetPasswordResponseDto> resetPassword(@Valid @RequestBody ResetPasswordRequestDto resetPasswordRequestDto) {
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/admin")
    public ResponseEntity<AdminTokenResponse> adminLogin(@RequestBody @Valid AdminLoginRequest adminLoginReq) {
        final var tokenResponseOptional = authService.adminLogin(adminLoginReq);

        return tokenResponseOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().body(
                        AdminTokenResponse.builder()
                                .build()
                ));
    }
}
