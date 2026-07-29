package com.collabpulse.chatservice.controller;


import com.collabpulse.chatservice.model.Message;
import com.collabpulse.chatservice.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class
MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<Message>  sendMessage(@RequestBody Message message) {
        Message savedMessage = messageService.sendMessage(message);
        return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);

    }
    @GetMapping("/history")
    public ResponseEntity<List<Message>> getChatHistory(
            @RequestHeader("X-User-Id") Long myUserId,
            @RequestParam Long withUserId) {
        List<Message> history = messageService.getChatHistory(myUserId, withUserId);
        return ResponseEntity.ok(history);
    }

    }

