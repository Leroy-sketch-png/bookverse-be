package com.example.bookverseserver.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Conversation summary for inbox list.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationSummaryResponse {
    
    Long id;
    
    /**
     * The other party in the conversation (not the current user).
     */
    ParticipantInfo otherParty;
    
    /**
     * Linked listing info (if any).
     */
    ListingPreview listing;
    
    /**
     * Last message preview.
     */
    LastMessageInfo lastMessage;
    
    /**
     * Unread count for current user.
     */
    int unreadCount;
    
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ParticipantInfo {
        Long id;
        String name;
        String avatar;
        boolean isPro;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ListingPreview {
        Long id;
        String title;
        String thumbnail;
        java.math.BigDecimal price;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class LastMessageInfo {
        String preview;
        LocalDateTime sentAt;
        boolean isFromMe;
    }
}
