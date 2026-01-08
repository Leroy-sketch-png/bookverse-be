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
 * Order DTO for buyer view - matches Vision API_CONTRACTS.md
 * 
 * FE expects: seller, items, summary (nested), paymentMethod, paymentStatus, shipping (nested), timeline
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDTO {
  Long id;
  String orderNumber;
  OrderStatus status;
  
  // Seller info (Vision: nested object)
  SellerInfo seller;
  
  // Order items
  List<OrderItemDTO> items;
  
  // Financial summary (Vision: nested summary object)
  OrderSummary summary;
  
  // Payment info
  String paymentMethod;
  PaymentStatus paymentStatus;
  
  // Shipping info (Vision: nested shipping object)
  ShippingInfo shipping;
  
  // Order timeline
  List<OrderTimelineDTO> timeline;
  
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
}
