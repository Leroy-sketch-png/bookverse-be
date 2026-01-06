package com.example.bookverseserver.dto.response.Order;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckoutResponse {
  Long sessionId;
  Long orderId;
  String paymentIntentId;
  String clientSecret;
  BigDecimal amount;
  String currency;
  String status;
  LocalDateTime expiresAt;
}
