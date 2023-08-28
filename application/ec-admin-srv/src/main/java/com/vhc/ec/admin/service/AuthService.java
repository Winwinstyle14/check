package com.vhc.ec.admin.service;

import com.vhc.ec.admin.constant.BaseStatus;
import com.vhc.ec.admin.dto.LoginResponse;
import com.vhc.ec.admin.dto.LoginRequest;
import com.vhc.ec.admin.dto.UserViewDto;
import com.vhc.ec.admin.repository.UserRepository;
import com.vhc.ec.admin.util.BlindSslSocketFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ModelMapper mapper;

    @Value("${vhc.ec.ldap}")
    private String ldapUrl;

    public LoginResponse auth(LoginRequest loginRequest) {
        final var userOptional = userRepository.findByEmail(loginRequest.getEmail());
        String code = "";

        if (userOptional.isPresent()) {
            final var user = userOptional.get();
            boolean loginSuccess = false;

            if (user.getStatus() == BaseStatus.ACTIVE) {

                if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                    loginSuccess = true;
                } else {
                    code = "03";
                }

                if (loginSuccess) {
                    return LoginResponse.builder()
                            .success(true)
                            .code("00")
                            .user(mapper.map(user, UserViewDto.class))
                            .build();
                }
            } else {
                code = "01"; // user in active
            }
        }

        return LoginResponse.builder()
                .success(false)
                .code(code)
                .build();
    }
}
