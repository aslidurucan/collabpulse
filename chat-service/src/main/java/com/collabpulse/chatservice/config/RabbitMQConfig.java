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

    public static final String DLQ_EXCHANGE_NAME = "collabpulse.chat.exchange.dlx";
    public static final String DLQ_QUEUE_NAME = "collabpulse.chat.messages.dlq";
    public static final String DLQ_ROUTING_KEY = "collabpulse.chat.routingKey.dlq";

    @Bean
    public Queue chatQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding chatBinding(Queue chatQueue, DirectExchange chatExchange) {
        return BindingBuilder.bind(chatQueue).to(chatExchange).with(ROUTING_KEY);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ_QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLQ_EXCHANGE_NAME);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}