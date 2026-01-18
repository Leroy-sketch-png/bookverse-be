package com.example.bookverseserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Request for content moderation check
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModerationRequest {
    
    @NotBlank(message = "CONTENT_REQUIRED")
    @Size(min = 1, max = 10000, message = "CONTENT_TOO_LONG")
    String text;
    
    /**
     * Type of content: review, listing_title, listing_description, message
     */
    @Builder.Default
    String contentType = "review";
    
    /**
     * User reputation score (0-100), affects threshold sensitivity
     */
    @Builder.Default
    Integer userReputation = 50;
    
    /**
     * Optional additional context for AI moderation
     */
    @Size(max = 500)
    String context;
}
