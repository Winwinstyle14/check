package com.vhc.ec.bpmn.service;

import com.vhc.ec.bpmn.dto.SignFlowNotifyRequest;
import com.vhc.ec.bpmn.dto.SignFlowNotifyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    @Value("${vhc.micro-services.notification.api-url}")
    private String notificationUrl;

    final RestTemplate restTemplate;

    public SignFlowNotifyResponse sendSignFlowNotify(SignFlowNotifyRequest signFlowNotifyRequest) {

        return restTemplate.postForEntity(notificationUrl, signFlowNotifyRequest, SignFlowNotifyResponse.class).getBody();
    }
}
