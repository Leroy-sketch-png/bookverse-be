package com.example.bookverseserver.dto.request.Order;

import com.example.bookverseserver.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateOrderStatusRequest {
    
    @NotNull(message = "Status is required")
    OrderStatus status; // Only allow PROCESSING or SHIPPED
    
    String trackingNumber; // Required when status is SHIPPED
}
