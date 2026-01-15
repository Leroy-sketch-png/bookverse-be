package com.example.bookverseserver.dto.request;

import com.example.bookverseserver.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request to send a message in a conversation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendMessageRequest {
    
    @NotBlank(message = "MESSAGE_REQUIRED")
    @Size(max = 2000, message = "MESSAGE_TOO_LONG")
    String message;
    
    /**
     * Type of message. Defaults to TEXT.
     */
    MessageType type;
    
    /**
     * Related entity ID for LISTING_SHARE or ORDER_UPDATE types.
     */
    Long relatedId;
}
