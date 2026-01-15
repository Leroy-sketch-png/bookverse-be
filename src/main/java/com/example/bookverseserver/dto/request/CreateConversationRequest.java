package com.example.bookverseserver.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request to create or get an existing conversation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateConversationRequest {
    
    @NotNull(message = "SELLER_ID_REQUIRED")
    Long sellerId;
    
    /**
     * Optional: Link conversation to a specific listing.
     */
    Long listingId;
    
    /**
     * Optional: Initial message to send when creating conversation.
     */
    String initialMessage;
}
