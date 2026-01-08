package com.example.bookverseserver.dto.response.Order;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Response after completing checkout - contains Stripe payment intent details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompleteCheckoutResponse {
    Long orderId;
    String orderNumber;
    
    // Stripe payment intent for frontend
    PaymentIntentDTO paymentIntent;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentIntentDTO {
        String id;
        String clientSecret;
        BigDecimal amount;
        String currency;
        String status;
        String publishableKey; // Stripe publishable key for frontend
    }
}
