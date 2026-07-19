package com.collabpulse.chatservice.consumer;

import com.collabpulse.chatservice.config.RabbitMQConfig;
import com.collabpulse.chatservice.model.Message;
import com.collabpulse.chatservice.service.MessageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.collabpulse.chatservice.client.UserClient;

import java.util.concurrent.TimeUnit;

@Component
public class ChatConsumer {

    private static final String VALIDATION_CACHE_PREFIX = "user:valid:";
    private static final long VALIDATION_CACHE_TTL_MINUTES = 5;

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserClient userClient;
    private final StringRedisTemplate redisTemplate;

    public ChatConsumer(MessageService messageService, SimpMessagingTemplate messagingTemplate,
                         UserClient userClient, StringRedisTemplate redisTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.userClient = userClient;
        this.redisTemplate = redisTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeChatMessage(Message message) {
        System.out.println("RabbitMQ'dan iş emri çekildi, işleniyor: " + message.getId());

        if (isUserValid(message.getSenderId())) {
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

    private boolean isUserValid(Long senderId) {
        String cacheKey = VALIDATION_CACHE_PREFIX + senderId;
        String cachedValue = redisTemplate.opsForValue().get(cacheKey);

        if (cachedValue != null) {
            System.out.println("Doğrulama cache'ten geldi (Feign atlandı). Key: " + cacheKey);
            return Boolean.parseBoolean(cachedValue);
        }

        boolean isValid = Boolean.TRUE.equals(userClient.validateUser(String.valueOf(senderId)));
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(isValid), VALIDATION_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return isValid;
    }
}