package com.vhc.ec.contract.service;

import com.vhc.ec.contract.dto.LoginRequest;
import com.vhc.ec.contract.dto.LoginResponse;
import com.vhc.ec.contract.dto.RecipientDto;
import com.vhc.ec.contract.repository.ContractRepository;
import com.vhc.ec.contract.repository.RecipientRepository;
import com.vhc.ec.contract.repository.ShareRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    private final RecipientRepository recipientRepository;
    private final ShareRepository shareRepository;
    private final ContractRepository contractRepository;
    private final ModelMapper modelMapper;

    /**
     * Đăng nhập qua tài khoản tạm của người xử lý hồ sơ
     *
     * @param username Tên đăng nhập của người xử lý hồ sơ
     * @param password Mật khẩu tạm của người xử lý hồ sơ
     * @return {@link RecipientDto}
     */
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        String username = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        final var recipientOptional = recipientRepository.findFirstByUsernameAndPassword(
                username, password
        );

        int id = 0;
        String email = null;
        String name = null;
        int contractId = 0;

        if (recipientOptional.isEmpty()) {
            // user have received contract
            final var share = shareRepository.findFirstByEmailAndToken(username, password).orElse(null);
            if (share == null) {
                return ResponseEntity.ok(LoginResponse.builder().success(false).build());
            }

            id = share.getId();
            email = share.getEmail();
            name = share.getEmail();
            contractId = share.getContractId();

        } else {
            var recipient = recipientOptional.get();
            id = recipient.getId();
            email = recipient.getEmail();
            name = recipient.getName();
            contractId = recipient.getParticipant().getContractId();
        }

        if (contractId != loginRequest.getContractId()) {
            return ResponseEntity.ok(LoginResponse.builder().success(false).build());
        }

        return ResponseEntity
                .ok(LoginResponse.builder()
                        .success(true)
                        .recipient(
                                LoginResponse.RecipientResponse.builder()
                                        .id(id)
                                        .email(email)
                                        .name(name)
                                        .username(username)
                                        .build())
                        .build());
    }
}
