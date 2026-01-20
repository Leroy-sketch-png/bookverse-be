package com.example.bookverseserver.dto.request.Order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for completing a checkout session.
 * Added validation for payment method.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompleteCheckoutRequest {
    
    @NotBlank(message = "PAYMENT_METHOD_REQUIRED")
    @Pattern(regexp = "^(stripe|cod|bank_transfer)$", message = "INVALID_PAYMENT_METHOD")
    @Builder.Default
    String paymentMethod = "stripe";
}
