package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.request.CreateConversationRequest;
import com.example.bookverseserver.dto.request.SendMessageRequest;
import com.example.bookverseserver.dto.response.ConversationDetailResponse;
import com.example.bookverseserver.dto.response.ConversationSummaryResponse;
import com.example.bookverseserver.dto.response.MessageResponse;
import com.example.bookverseserver.service.MessagingService;
import com.example.bookverseserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Messaging", description = "Buyer-seller messaging system")
public class ConversationController {
    
    MessagingService messagingService;
    SecurityUtils securityUtils;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Conversation Endpoints
    // ─────────────────────────────────────────────────────────────────────────
    
    @GetMapping
    @Operation(summary = "Get user's conversations", 
               description = "Returns paginated list of conversations for the current user")
    public ApiResponse<Page<ConversationSummaryResponse>> getConversations(
            Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = securityUtils.getCurrentUserId(auth);
        Page<ConversationSummaryResponse> conversations = messagingService.getConversations(
            userId, PageRequest.of(Math.max(0, page - 1), size));
        
        return ApiResponse.<Page<ConversationSummaryResponse>>builder()
            .result(conversations)
            .build();
    }
    
    @PostMapping
    @Operation(summary = "Create or get conversation", 
               description = "Creates a new conversation with a seller, or returns existing one")
    public ApiResponse<ConversationSummaryResponse> createConversation(
            Authentication auth,
            @Valid @RequestBody CreateConversationRequest request) {
        
        Long userId = securityUtils.getCurrentUserId(auth);
        ConversationSummaryResponse conversation = messagingService.getOrCreateConversation(userId, request);
        
        return ApiResponse.<ConversationSummaryResponse>builder()
            .result(conversation)
            .build();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get conversation detail", 
               description = "Returns conversation with recent messages")
    public ApiResponse<ConversationDetailResponse> getConversation(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") int messageLimit) {
        
        Long userId = securityUtils.getCurrentUserId(auth);
        ConversationDetailResponse conversation = messagingService.getConversationDetail(id, userId, messageLimit);
        
        return ApiResponse.<ConversationDetailResponse>builder()
            .result(conversation)
            .build();
    }
    
    @PostMapping("/{id}/read")
    @Operation(summary = "Mark conversation as read", 
               description = "Marks all messages in the conversation as read for current user")
    public ApiResponse<Map<String, Boolean>> markAsRead(
            Authentication auth,
            @PathVariable Long id) {
        
        Long userId = securityUtils.getCurrentUserId(auth);
        messagingService.markAsRead(id, userId);
        
        return ApiResponse.<Map<String, Boolean>>builder()
            .result(Map.of("success", true))
            .build();
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "Get total unread count", 
               description = "Returns total unread message count across all conversations")
    public ApiResponse<Map<String, Integer>> getUnreadCount(Authentication auth) {
        Long userId = securityUtils.getCurrentUserId(auth);
        int count = messagingService.getTotalUnreadCount(userId);
        
        return ApiResponse.<Map<String, Integer>>builder()
            .result(Map.of("count", count))
            .build();
    }
    
    // ─────────────────────────────────────────────────────────────────────────
    // Message Endpoints
    // ─────────────────────────────────────────────────────────────────────────
    
    @GetMapping("/{id}/messages")
    @Operation(summary = "Get conversation messages", 
               description = "Returns paginated messages for a conversation")
    public ApiResponse<Page<MessageResponse>> getMessages(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Long userId = securityUtils.getCurrentUserId(auth);
        Page<MessageResponse> messages = messagingService.getMessages(id, userId, PageRequest.of(Math.max(0, page - 1), size));
        
        return ApiResponse.<Page<MessageResponse>>builder()
            .result(messages)
            .build();
    }
    
    @PostMapping("/{id}/messages")
    @Operation(summary = "Send a message", 
               description = "Sends a message in the conversation")
    public ApiResponse<MessageResponse> sendMessage(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request) {
        
        Long userId = securityUtils.getCurrentUserId(auth);
        MessageResponse message = messagingService.sendMessage(id, userId, request);
        
        return ApiResponse.<MessageResponse>builder()
            .result(message)
            .build();
    }
}
