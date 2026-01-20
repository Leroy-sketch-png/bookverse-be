package com.example.bookverseserver.dto.request.Transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request DTO for creating a payment intent.
 * Added validation annotations to prevent invalid data from reaching Stripe.
 */
@Data
public class CreatePaymentIntentRequest {
    
    @NotNull(message = "ORDER_ID_REQUIRED")
    private Long orderId;
    
    @NotBlank(message = "PAYMENT_METHOD_REQUIRED")
    @Pattern(regexp = "^(STRIPE|BANK_TRANSFER|COD)$", message = "INVALID_PAYMENT_METHOD")
    private String paymentMethod; // e.g., "STRIPE", "BANK_TRANSFER", "COD"
    
    private String returnUrl;
    
    private String cancelUrl;
}