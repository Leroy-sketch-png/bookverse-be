package com.example.bookverseserver.dto.request.Order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for cancelling an order.
 * Added validation for reason field.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CancelOrderRequest {
  
  @NotBlank(message = "CANCELLATION_REASON_REQUIRED")
  @Size(min = 10, max = 500, message = "CANCELLATION_REASON_LENGTH")
  String reason;
}
