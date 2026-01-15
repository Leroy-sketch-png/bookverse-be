package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.SendMessageRequest;
import com.example.bookverseserver.dto.response.MessageResponse;
import com.example.bookverseserver.service.MessagingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket controller for real-time messaging.
 * Handles STOMP messages for chat functionality.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatWebSocketController {
    
    MessagingService messagingService;
    SimpMessagingTemplate messagingTemplate;
    
    /**
     * Handle incoming chat messages.
     * Client sends to: /app/chat/{conversationId}
     * Server broadcasts to: /topic/conversation/{conversationId}
     */
    @MessageMapping("/chat/{conversationId}")
    public void sendMessage(
            @DestinationVariable Long conversationId,
            @Payload SendMessageRequest request,
            Principal principal) {
        
        if (principal == null) {
            log.warn("Received message without authenticated user");
            return;
        }
        
        Long senderId = Long.parseLong(principal.getName());
        log.info("WebSocket message received: conversation={}, sender={}", conversationId, senderId);
        
        try {
            // Save message to database
            MessageResponse message = messagingService.sendMessage(conversationId, senderId, request);
            
            // Broadcast to all subscribers of this conversation
            String destination = "/topic/conversation/" + conversationId;
            messagingTemplate.convertAndSend(destination, message);
            
            log.info("Message broadcast to {}", destination);
            
            // Also notify each participant individually (for unread badge updates)
            // This uses user-specific destinations
            notifyParticipants(conversationId, message);
            
        } catch (Exception e) {
            log.error("Error processing WebSocket message: {}", e.getMessage(), e);
            // Send error back to sender
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                new ErrorMessage("Failed to send message: " + e.getMessage())
            );
        }
    }
    
    /**
     * Notify participants of new message (for unread updates).
     */
    private void notifyParticipants(Long conversationId, MessageResponse message) {
        // The unread count update will be handled by the frontend when receiving the message
        // This is a placeholder for additional notification logic if needed
        // e.g., push notifications, email notifications for offline users
    }
    
    /**
     * Handle typing indicators.
     * Client sends to: /app/typing/{conversationId}
     */
    @MessageMapping("/typing/{conversationId}")
    public void handleTyping(
            @DestinationVariable Long conversationId,
            @Payload TypingIndicator indicator,
            Principal principal) {
        
        if (principal == null) return;
        
        Long userId = Long.parseLong(principal.getName());
        indicator.setUserId(userId);
        
        // Broadcast typing status to conversation
        messagingTemplate.convertAndSend(
            "/topic/conversation/" + conversationId + "/typing",
            indicator
        );
    }
    
    // ─────────────────────────────────────────────────────────────────────────
    // Inner Classes for WebSocket Messages
    // ─────────────────────────────────────────────────────────────────────────
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TypingIndicator {
        private Long userId;
        private String userName;
        private boolean isTyping;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ErrorMessage {
        private String message;
    }
}
