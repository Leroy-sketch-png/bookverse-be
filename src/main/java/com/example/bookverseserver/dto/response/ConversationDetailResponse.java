package com.example.bookverseserver.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Detailed conversation response including messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationDetailResponse {
    
    Long id;
    
    /**
     * The other party in the conversation.
     */
    ConversationSummaryResponse.ParticipantInfo otherParty;
    
    /**
     * Linked listing info (if any).
     */
    ConversationSummaryResponse.ListingPreview listing;
    
    /**
     * Linked order info (if any).
     */
    OrderPreview order;
    
    /**
     * Messages in the conversation (paginated).
     */
    List<MessageResponse> messages;
    
    /**
     * Whether there are more messages to load.
     */
    boolean hasMoreMessages;
    
    /**
     * Unread count for current user.
     */
    int unreadCount;
    
    LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderPreview {
        Long id;
        String orderNumber;
        String status;
    }
}
