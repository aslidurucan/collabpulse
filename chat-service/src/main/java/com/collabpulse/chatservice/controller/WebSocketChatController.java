package com.collabpulse.chatservice.controller;

import com.collabpulse.chatservice.config.RabbitMQConfig;
import com.collabpulse.chatservice.model.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketChatController {

    private final RabbitTemplate rabbitTemplate;

    public WebSocketChatController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @MessageMapping("/chat.send")
    public void receiveAndQueueMessage(@Payload Message message) {
        System.out.println("Canlı hattan yoğun istek geldi. Kuyruğa fırlatılıyor. ID: " + message.getId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                message
        );
    }
}