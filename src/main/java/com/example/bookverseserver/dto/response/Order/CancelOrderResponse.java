package com.example.bookverseserver.dto.response.Order;

import com.example.bookverseserver.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CancelOrderResponse {
  Long orderId;
  OrderStatus status;
  BigDecimal refundAmount;
  String refundStatus;
  LocalDateTime cancelledAt;
}
