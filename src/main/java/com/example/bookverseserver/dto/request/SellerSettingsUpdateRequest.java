package com.example.bookverseserver.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

/**
 * Request DTO for updating seller settings.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerSettingsUpdateRequest {
    
    /**
     * Shipping preferences:
     * - standardShipping (boolean)
     * - expressShipping (boolean)
     * - localPickup (boolean)
     * - freeShippingThreshold (number)
     * - freeShippingEnabled (boolean)
     */
    Map<String, Object> shipping;
    
    /**
     * Notification preferences:
     * - emailNewOrder (boolean)
     * - emailOrderShipped (boolean)
     * - emailLowStock (boolean)
     * - emailWeeklyReport (boolean)
     * - pushNewOrder (boolean)
     * - pushMessages (boolean)
     */
    Map<String, Object> notifications;
    
    /**
     * Privacy settings:
     * - showPhone (boolean)
     * - allowMessages (boolean)
     * - showStats (boolean)
     */
    Map<String, Object> privacy;
}
