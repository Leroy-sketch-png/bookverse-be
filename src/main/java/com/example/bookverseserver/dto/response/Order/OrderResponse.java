package com.example.bookverseserver.dto.response.Order;

import com.example.bookverseserver.dto.response.ShippingAddress.ShippingAddressResponse;
import com.example.bookverseserver.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    UUID id;
    String orderNumber;
    LocalDateTime createdAt;
    OrderStatus status;
    List<OrderItemResponse> items;
    BigDecimal totalAmount;
    BigDecimal finalAmount;
    DiscountInfo discount;
    ShippingAddressResponse shippingAddress;
    String trackingNumber;
    String trackingUrl;
    LocalDateTime shippedAt;
    LocalDateTime deliveredAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DiscountInfo {
        String type; // "fixed" or "percentage"
        BigDecimal value;
    }
}
