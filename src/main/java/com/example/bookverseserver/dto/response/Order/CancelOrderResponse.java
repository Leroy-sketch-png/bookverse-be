package com.example.bookverseserver.dto.response.Order;

import com.example.bookverseserver.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CancelOrderResponse {
  UUID orderId;
  OrderStatus status;
  BigDecimal refundAmount;
  String refundStatus;
  LocalDateTime cancelledAt;
}
