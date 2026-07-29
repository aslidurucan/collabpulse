package com.collabpulse.chatservice.service;

import com.collabpulse.chatservice.model.Message;
import com.collabpulse.chatservice.repository.MessageRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message sendMessage(Message message) {
        // 1. ID kontrolü ve ataması
        if (message.getId() == null || message.getId().trim().isEmpty()) {
            message.setId(UUID.randomUUID().toString());
        }

        // 2. GÜVENLİ IDEMPOTENCY KONTROLÜ (Clean Code Standartı)
        // existsById + findById.get() yerine doğrudan findById yapıp Optional'ı sorguluyoruz.
        // Bu sayede veritabanına iki kez gitmek yerine tek sorguyla işi bitirip performansı artırıyoruz.
        Optional<Message> existingMessage = messageRepository.findById(message.getId());
        if (existingMessage.isPresent()) {
            // Eğer mesaj zaten varsa, o mesajı güvenle dönüyoruz
            return existingMessage.get();
        }

        // 3. Yeni mesaj alanlarını dolduruyoruz
        message.setTimestamp(LocalDateTime.now());
        message.setStatus("SENT");

        // 4. Docker MongoDB'ye kaydet
        return messageRepository.save(message);
    }

    public List<Message> getChatHistory(Long myUserId, Long otherUserId) {
        return messageRepository.findConversation(myUserId, otherUserId, Sort.by("timestamp").ascending());
    }
}
