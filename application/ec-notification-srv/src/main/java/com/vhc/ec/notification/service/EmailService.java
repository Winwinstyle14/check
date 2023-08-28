package com.vhc.ec.notification.service;

import com.vhc.ec.notification.entity.Email;
import com.vhc.ec.notification.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    final EmailRepository emailRepository;

    public Email save(Email email) {

        return emailRepository.save(email);
    }
}
