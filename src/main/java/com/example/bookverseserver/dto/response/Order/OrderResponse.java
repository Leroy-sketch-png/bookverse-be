package com.example.bookverseserver.dto.response.Order;

import com.example.bookverseserver.dto.response.ShippingAddress.ShippingAddressResponse;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Response - matches Vision API_CONTRACTS.md buyer order view
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long id;
    String orderNumber;
    OrderStatus status;
    
    // Seller info (Vision requires nested seller object)
    SellerInfo seller;
    
    // Order items
    List<OrderItemResponse> items;
    
    // Financial summary (Vision requires nested summary object)
    OrderSummary summary;
    
    // Payment info
    String paymentMethod;
    PaymentStatus paymentStatus;
    
    // Shipping info (Vision requires nested shipping object)
    ShippingInfo shipping;
    
    // Order timeline
    List<TimelineEntry> timeline;
    
    String estimatedDelivery;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SellerInfo {
        Long id;
        String name;
        String avatar;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderSummary {
        BigDecimal subtotal;
        BigDecimal shipping;
        BigDecimal discount;
        BigDecimal total;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShippingInfo {
        ShippingAddressResponse address;
        String carrier;
        String trackingNumber;
        String trackingUrl;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimelineEntry {
        String status;
        String timestamp;
        String note;
    }
}
