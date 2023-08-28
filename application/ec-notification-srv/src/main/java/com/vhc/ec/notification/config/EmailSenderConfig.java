package com.vhc.ec.notification.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class EmailSenderConfig {

    @Value("${spring.mail.username}")
    private String username;
}
