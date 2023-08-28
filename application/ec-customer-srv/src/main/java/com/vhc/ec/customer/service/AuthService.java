package com.vhc.ec.customer.service;

import com.vhc.ec.customer.defination.BaseStatus;
import com.vhc.ec.customer.dto.CustomerDto;
import com.vhc.ec.customer.dto.LoginRequestDto;
import com.vhc.ec.customer.dto.LoginResponseDto;
import com.vhc.ec.customer.repository.CustomerRepository;
import com.vhc.ec.customer.util.BlindSslSocketFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    final CustomerRepository customerRepository;
    final ModelMapper modelMapper;
    final PasswordEncoder passwordEncoder;

    @Value("${vhc.ec.ldap}")
    private String ldapUrl;

    @Value("${vhc.ec.api.version.site}")
    private String BE_SITE;

    /**
     * Find customer and check password is matched
     *
     * @param loginRequestDto
     * @return
     */
    public LoginResponseDto signIn(LoginRequestDto loginRequestDto) {
        final var customerOptional = customerRepository
                .findTopByEmail(loginRequestDto.getEmail());

        LoginResponseDto loginResponseDto = null;

        String code = null;
        if (customerOptional.isPresent()) {
            final var customer = customerOptional.get();
            var today = LocalDate.now();
            var org = customer.getOrganization();

            code = "02"; // organization in active
            if ( org != null &&
                    (org.getStatus() == BaseStatus.ACTIVE ||
                            (org.getStatus() == BaseStatus.PENDING &&
                            		today.compareTo(org.getStopServiceDay().plusMonths(org.getEndTime())) < 0
                            ) ||

                            (org.getStatus() == BaseStatus.CANCEL &&
                            		today.compareTo(org.getStopServiceDay().plusMonths(org.getEndTime())) < 0
                            )
                    )
            ) {

                if (customer.getStatus() == BaseStatus.ACTIVE) {
                    boolean loginOk = false;

                    if(BE_SITE.equals("nb")){
                        if (customer.getEmail().contains("@mobifone.vn")) {
                            String account = customer.getEmail().replace("@mobifone.vn", "");

                            if (BlindSslSocketFactory.authentication(ldapUrl, account, loginRequestDto.getPassword())) {
                                loginOk = true;
                            } else {
                                code = "03";
                            }
                            log.info("login {} is {}", account, loginOk);
                        } else {
                            // check password is matched
                            if (passwordEncoder.matches(loginRequestDto.getPassword(), customerOptional.get().getPassword())) {
                                loginOk = true;
                            } else {
                                code = "03";
                            }
                        }
                    }

                    if(BE_SITE.equals("kd")){
                        // check password is matched
                        if (passwordEncoder.matches(loginRequestDto.getPassword(), customerOptional.get().getPassword())) {
                            loginOk = true;
                        } else {
                            code = "03";
                        }
                    }

                    /**
                     * login is success
                     */
                    if (loginOk) {
                        loginResponseDto = LoginResponseDto.builder()
                                .success(true).code("00")
                                .customer(
                                        modelMapper
                                                .map(customer, CustomerDto.class)
                                )
                                .build();

                        return loginResponseDto;
                    }
                } else {
                    code = "01"; // user in active
                }
            }
        }

        loginResponseDto = LoginResponseDto.builder()
                .success(false)
                .code(code)
                .customer(null)
                .build();

        return loginResponseDto;
    }
}
