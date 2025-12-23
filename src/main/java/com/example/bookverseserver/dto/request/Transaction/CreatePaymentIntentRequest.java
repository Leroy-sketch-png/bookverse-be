package com.example.bookverseserver.dto.request.Transaction;

import lombok.Data;

@Data
public class CreatePaymentIntentRequest {
    private Long orderId;
    private String paymentMethod; // e.g., "STRIPE"
    private String returnUrl;
    private String cancelUrl;
}