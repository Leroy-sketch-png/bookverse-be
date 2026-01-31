package com.example.bookverseserver.dto.request.Transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for verifying a payment intent.
 * Added validation annotations to prevent invalid Stripe IDs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentRequest {
    
    @NotBlank(message = "PAYMENT_INTENT_ID_REQUIRED")
    @Pattern(regexp = "^pi_[a-zA-Z0-9_]+$", message = "INVALID_PAYMENT_INTENT_ID_FORMAT")
    private String paymentIntentId;
    
    @NotNull(message = "ORDER_ID_REQUIRED")
    private Long orderId;
}