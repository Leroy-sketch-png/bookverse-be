package com.example.bookverseserver.dto.response.Transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentResponse {
    private String paymentIntentId;
    private String clientSecret;
    private long amount;
    private String currency;
    private String status;
    private String publishableKey;
}