package com.example.bookverseserver.dto.request.Transaction;

import lombok.Data;

import java.util.UUID;

@Data
public class CreatePaymentIntentRequest {
    private UUID orderId;
    private String paymentMethod; // e.g., "STRIPE"
    private String returnUrl;
    private String cancelUrl;
}