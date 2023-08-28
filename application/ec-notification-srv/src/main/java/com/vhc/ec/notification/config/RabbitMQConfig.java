package com.vhc.ec.notification.config;

import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.email.queue}")
    private String emailQueue;

    @Value("${rabbitmq.email.routing}")
    private String emailRouting;

    @Value("${rabbitmq.sms.queue}")
    private String smsQueue;

    @Value("${rabbitmq.sms.routing}")
    private String smsRouting;

    @Bean
    Queue emailQueue() {
        return new Queue(emailQueue, Boolean.FALSE);
    }

    @Bean
    Queue smsQueue() {
        return new Queue(smsQueue, Boolean.FALSE);
    }

    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    Binding bindingEmail(final Queue emailQueue, final TopicExchange topicExchange) {
        return BindingBuilder.bind(emailQueue).to(topicExchange).with(emailRouting);
    }

    @Bean
    Binding bindingSms(final Queue smsQueue, final TopicExchange topicExchange) {
        return BindingBuilder.bind(smsQueue).to(topicExchange).with(smsRouting);
    }
}
