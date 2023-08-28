package com.vhc.ec.customer.controller;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.customer.dto.LoginRequestDto;
import com.vhc.ec.customer.dto.LoginResponseDto;
import com.vhc.ec.customer.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ResponseStatus(HttpStatus.OK)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping
    public LoginResponseDto signIn(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return authService.signIn(loginRequestDto);
    }
}
