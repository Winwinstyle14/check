package com.vhc.ec.notification.service;

import com.vhc.ec.notification.entity.Message;
import com.vhc.ec.notification.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    final MessageRepository messageRepository;

    public Optional<Message> findByCode(String code) {

        return messageRepository.findTopByCode(code);
    }
}
