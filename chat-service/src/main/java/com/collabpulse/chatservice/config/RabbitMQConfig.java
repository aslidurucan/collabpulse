package com.collabpulse.chatservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "collabpulse.chat.messages";
    public static final String EXCHANGE_NAME = "collabpulse.chat.exchange";
    public static final String ROUTING_KEY = "collabpulse.chat.routingKey";

    @Bean
    public Queue chatQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding chatBinding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}