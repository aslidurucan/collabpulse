package com.collabpulse.chatservice.consumer;

import com.collabpulse.chatservice.config.RabbitMQConfig;
import com.collabpulse.chatservice.model.Message;
import com.collabpulse.chatservice.service.MessageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.collabpulse.chatservice.client.UserClient;

@Component
public class ChatConsumer {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserClient userClient;

    public ChatConsumer(MessageService messageService, SimpMessagingTemplate messagingTemplate, UserClient userClient) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.userClient = userClient;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeChatMessage(Message message) {
        System.out.println("RabbitMQ'dan iş emri çekildi, işleniyor: " + message.getId());
        Boolean isValid = userClient.validateUser(String.valueOf(message.getSenderId()));

        if (Boolean.TRUE.equals(isValid)) {
            System.out.println("Kullanıcı doğrulandı. İşlem devam ediyor.");
            Message savedMessage = messageService.sendMessage(message);
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(savedMessage.getReceiverId()),
                    "/queue/messages",
                    savedMessage
            );
        } else {
            System.out.println("Kritik Hata: Geçersiz kullanıcı isteği reddedildi! ID: " + message.getSenderId());
        }
    }
}