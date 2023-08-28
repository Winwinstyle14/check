package com.vhc.ec.auth.controller;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.auth.dto.ValidateTokenRequestDto;
import com.vhc.ec.auth.dto.ValidateTokenResponseDto;
import com.vhc.ec.auth.service.CustomerTokenService;
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
@RequestMapping("/token")
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ResponseStatus(HttpStatus.OK)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class TokenController {

    private final CustomerTokenService tokenService;

    @PostMapping
    public ValidateTokenResponseDto validate(
            @Valid @RequestBody ValidateTokenRequestDto validateTokenRequestDto) {
        return tokenService.validate(
                validateTokenRequestDto.getAccessToken()
        );
    }

}
