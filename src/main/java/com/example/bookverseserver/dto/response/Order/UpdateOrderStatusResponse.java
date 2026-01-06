package com.example.bookverseserver.dto.response.Order;

import com.example.bookverseserver.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Response DTO for seller order status update.
 * Per Vision API_CONTRACTS.md - PATCH /orders/:orderId/status response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateOrderStatusResponse {
    Long id;
    OrderStatus status;
    String trackingNumber;
    String carrier;
    LocalDateTime updatedAt;
}
