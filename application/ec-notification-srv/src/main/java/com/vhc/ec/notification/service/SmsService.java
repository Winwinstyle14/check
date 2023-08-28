package com.vhc.ec.notification.service;

import org.springframework.stereotype.Service;

import com.vhc.ec.notification.entity.Sms;
import com.vhc.ec.notification.repository.SmsRepository;

import lombok.RequiredArgsConstructor;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class SmsService {

    final SmsRepository smsRepository;

    public Sms save(Sms sms) {

        return smsRepository.save(sms);
    }
}
