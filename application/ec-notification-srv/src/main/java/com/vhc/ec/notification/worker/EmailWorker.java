package com.vhc.ec.notification.worker;

import javax.mail.internet.MimeMessage;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhc.ec.notification.config.EmailSenderConfig;
import com.vhc.ec.notification.entity.Email;
import com.vhc.ec.notification.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailWorker {

    final Queue emailQueue;
    final JavaMailSender emailSender;
    final EmailSenderConfig emailSenderConfig;
    final EmailService emailService;

    ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "#{emailQueue.getName()}")
    public void getMsg(final String message) {

        log.info("email: " + message);
        Email email = null;
        		
        try {
            email = objectMapper.readValue(message, Email.class);

//            SimpleMailMessage mailMessage = new SimpleMailMessage();
//            mailMessage.setFrom("vhcjsc@gmail.com");
//            mailMessage.setTo(email.getRecipient());
//            mailMessage.setSubject(email.getSubject());
//            mailMessage.setText(email.getContent());
//            emailSender.send(mailMessage);

            MimeMessage mimeMessage = emailSender.createMimeMessage();
            mimeMessage.setFrom(emailSenderConfig.getUsername());
            
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            //mimeMessage.setContent(email.getContent(), "text/html"); /** Use this or below line **/
            helper.setText(email.getContent(), true); // Use this or above line.
            helper.setTo(email.getRecipient());
            helper.setSubject(email.getSubject());
            helper.setFrom(emailSenderConfig.getUsername());

            emailSender.send(mimeMessage);
            
            //cập nhật trạng thái gửi
            email.setStatus(1);
            
        } catch (Exception e) {
            log.error("error send email: {}", e);
            email.setStatus(-1);
        }
        
        if(email != null) {
        	emailService.save(email);
        } 
    }
}
