package com.collabpulse.chatservice.consumer;

import com.collabpulse.chatservice.config.RabbitMQConfig;
import com.collabpulse.chatservice.model.Message;
import com.collabpulse.chatservice.service.MessageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatConsumer {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatConsumer(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeChatMessage(Message message) {
        System.out.println("RabbitMQ'dan iş emri çekildi, işleniyor: " + message.getId());

        Message savedMessage = messageService.sendMessage(message);

        messagingTemplate.convertAndSendToUser(
                String.valueOf(savedMessage.getReceiverId()),
                "/queue/messages",
                savedMessage
        );
    }
}