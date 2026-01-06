package com.example.bookverseserver.dto.request.Order;

import com.example.bookverseserver.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for seller to update order status.
 * Per Vision API_CONTRACTS.md - PATCH /orders/:orderId/status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateOrderStatusRequest {
    
    @NotNull(message = "Status is required")
    OrderStatus status; // Seller can set: PROCESSING, SHIPPED, DELIVERED
    
    String trackingNumber; // Required when status is SHIPPED
    
    String carrier; // e.g., "Viettel Post", "GHTK", "J&T"
    
    String note; // Optional note for timeline entry
}
