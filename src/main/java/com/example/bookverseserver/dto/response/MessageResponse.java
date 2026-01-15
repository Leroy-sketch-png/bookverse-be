package com.example.bookverseserver.dto.response;

import com.example.bookverseserver.enums.MessageType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Single message response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageResponse {
    
    Long id;
    Long conversationId;
    
    /**
     * Sender info.
     */
    SenderInfo sender;
    
    /**
     * Message content.
     */
    String message;
    MessageType type;
    
    /**
     * Related entity ID (for LISTING_SHARE or ORDER_UPDATE).
     */
    Long relatedId;
    
    /**
     * Shared listing preview (for LISTING_SHARE type).
     */
    SharedListingInfo sharedListing;
    
    /**
     * Is this message from the current user?
     */
    boolean isFromMe;
    
    /**
     * When the message was read (null if unread).
     */
    LocalDateTime readAt;
    
    LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SenderInfo {
        Long id;
        String name;
        String avatar;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SharedListingInfo {
        Long id;
        String title;
        String thumbnail;
        java.math.BigDecimal price;
    }
}
