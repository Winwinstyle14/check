package com.vhc.ec.admin.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.vhc.ec.admin.dto.AccountNoticeRequest;
import com.vhc.ec.admin.dto.PageDto;
import com.vhc.ec.admin.dto.SaveUserDto;
import com.vhc.ec.admin.dto.UserViewDto;
import com.vhc.ec.admin.entity.User;
import com.vhc.ec.admin.exception.CustomException;
import com.vhc.ec.admin.exception.ErrorCode;
import com.vhc.ec.admin.repository.PermissionRepository;
import com.vhc.ec.admin.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final String notifyUrl = "http://ec-notification-srv/api/v1/internal/notification/customerAccountNotice";

    public UserViewDto create(SaveUserDto saveUserDto) {
        validate(0, saveUserDto);
        var user = modelMapper.map(saveUserDto, User.class);
        var permissionCodes = saveUserDto.getPermissions()
                .stream().map(p -> p.getCode())
                .collect(Collectors.toList());

        user.setPermissions(permissionRepository.findByCodeIn(permissionCodes));
        return create(user);
    }

    @Transactional
    private UserViewDto create(User user) {
        var password = user.getPassword();
        if (!StringUtils.hasText(password)) {
            // generate password
            password = RandomStringUtils.randomAlphabetic(6);
        }

        // using bcrypt hash password
        String bcryptPassword = passwordEncoder.encode(password);
        user.setPassword(bcryptPassword);
        final var created = userRepository.save(user);

        AccountNoticeRequest accountNoticeRequest;

        accountNoticeRequest = AccountNoticeRequest
                .builder()
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .userType("admin")
                .build();

        // sending email notice
        try {
            restTemplate.postForObject(notifyUrl, accountNoticeRequest, Object.class);
        } catch (Exception e) {
            log.error("error", e);
        }
        return modelMapper.map(created, UserViewDto.class);
    }

    @Transactional
    public UserViewDto updateUser(long id, SaveUserDto saveUserDto) {
        var user = userRepository.findById(id).orElseThrow(()  -> new CustomException(ErrorCode.USER_NOT_FOUND));
        validate(id, saveUserDto);
        user.setName(saveUserDto.getName());
        user.setPhone(saveUserDto.getPhone());
        user.setStatus(saveUserDto.getStatus());
        var permissionCodes = saveUserDto.getPermissions()
                .stream().map(p -> p.getCode())
                .collect(Collectors.toList());

        user.setPermissions(permissionRepository.findByCodeIn(permissionCodes));
        user = userRepository.save(user);
        return modelMapper.map(user, UserViewDto.class);
    }

    private void validate(long id, SaveUserDto saveUserDto) {
        if (id > 0) {
            userRepository.findById(id).orElseThrow(()  -> new CustomException(ErrorCode.USER_NOT_FOUND));
            userRepository
                    .findByPhoneAndIdNot(saveUserDto.getPhone(), id)
                    .ifPresent((cus) -> {
                        throw new CustomException(ErrorCode.PHONE_IS_EXISTED);
                    });
        } else {
            userRepository
                    .findByEmail(saveUserDto.getEmail())
                    .ifPresent((customer) -> {
                        throw new CustomException(ErrorCode.EMAIL_IS_EXISTED);
                    });

            userRepository
                    .findByPhone(saveUserDto.getPhone())
                    .ifPresent(cus -> {
                        throw new CustomException(ErrorCode.PHONE_IS_EXISTED);
                    });
        }
    }

    public UserViewDto getUserDetail(long id) {
        var user = userRepository.findById(id).orElseThrow(()  -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return modelMapper.map(user, UserViewDto.class);
    }

    public PageDto<UserViewDto> search(String name, String email, String phone, Pageable page) {
        var userPage = userRepository.search(name, email, phone, page);

        return modelMapper.map(
                userPage, new TypeToken<PageDto<UserViewDto>>() {
                }.getType()
        );
    }
    
    public List<UserViewDto> findAll() {
        var userPage = userRepository.findAll();

        return modelMapper.map(
                userPage, new TypeToken<List<UserViewDto>>() {
                }.getType()
        );
    }
}
