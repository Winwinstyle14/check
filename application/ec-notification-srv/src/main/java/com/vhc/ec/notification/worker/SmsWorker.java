package com.vhc.ec.notification.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhc.ec.notification.entity.Email;
import com.vhc.ec.notification.entity.Sms;
import com.vhc.ec.notification.smpp.SmppProxyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsWorker {

    final Queue smsQueue;
    final SmppProxyManager smppProxyManager;

    ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "#{smsQueue.getName()}")
    public void getMsg(final String message) {

        log.info("sms: " + message);

        try {
            Sms sms = objectMapper.readValue(message, Sms.class);

            int status = smppProxyManager.send(sms);

            log.info("Sms={}, status={}", sms, status);
        } catch (Exception e) {
            log.error("error", e);
        }
    }
}
