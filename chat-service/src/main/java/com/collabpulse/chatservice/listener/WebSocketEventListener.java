package com.collabpulse.chatservice.listener;

import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.TimeUnit;

@Component
public class WebSocketEventListener {

    private final StringRedisTemplate redisTemplate;

    public WebSocketEventListener(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = accessor.getFirstNativeHeader("userId");

        if (userId != null) {
            redisTemplate.opsForValue().set("user:status:" + userId, "ONLINE", 35, TimeUnit.SECONDS);
            System.out.println("Sahadaki kurye canlı hatta girdi. ID: " + userId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = (String) accessor.getSessionAttributes().get("userId");

        if (userId != null) {
            redisTemplate.delete("user:status:" + userId);
            System.out.println("Kurye hattan düştü veya offline oldu. ID: " + userId);
        }
    }
}