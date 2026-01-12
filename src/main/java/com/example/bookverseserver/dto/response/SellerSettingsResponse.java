package com.example.bookverseserver.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for seller settings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerSettingsResponse {
    
    Long id;
    
    /**
     * Shipping preferences
     */
    Map<String, Object> shipping;
    
    /**
     * Notification preferences
     */
    Map<String, Object> notifications;
    
    /**
     * Privacy settings
     */
    Map<String, Object> privacy;
    
    LocalDateTime updatedAt;
}
