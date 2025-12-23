package com.example.bookverseserver.dto.request.Transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentRequest {
    private String paymentIntentId;
    private Long orderId;
}