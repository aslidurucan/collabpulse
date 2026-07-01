package com.collabpulse.chatservice.controller;

import com.collabpulse.chatservice.model.Message;
import com.collabpulse.chatservice.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketChatController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketChatController(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void receiveAndBroadcastMessage(@Payload Message message) {
        Message savedMessage = messageService.sendMessage(message);

        messagingTemplate.convertAndSend(
                "/topic/chat." + savedMessage.getReceiverId(),
                savedMessage
        );
    }
}