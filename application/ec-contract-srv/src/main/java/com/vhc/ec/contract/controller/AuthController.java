package com.vhc.ec.contract.controller;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.LoginRequest;
import com.vhc.ec.contract.dto.LoginResponse;
import com.vhc.ec.contract.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

/**
 * Đăng nhập vào tài khoản của người xử lý hồ sơ
 * (trong trường hợp người dùng không định danh)
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/auth")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Khách hàng không định danh đăng nhập tới hệ thống,
     * sử dụng tài khoản tạm.
     *
     * @param loginRequest Thông tin đăng nhập tạm trên hệ thống
     * @return {@link LoginResponse}
     */
    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

}
