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
public class OrderDTO {
  UUID id;
  String orderNumber;
  OrderStatus status;
  List<OrderItemDTO> items;
  BigDecimal subtotal;
  BigDecimal tax;
  BigDecimal shipping;
  BigDecimal discount;
  BigDecimal total;
  String trackingNumber;
  String trackingUrl;
  String carrier;
  LocalDateTime estimatedDelivery;
  ShippingAddressResponse shippingAddress;
  ShippingAddressResponse billingAddress;
  String paymentMethod;
  String paymentStatus;
  List<OrderTimelineDTO> timeline;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}
