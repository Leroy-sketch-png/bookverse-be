package com.example.bookverseserver.dto.response.Transaction;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentVerificationResponse {
    private String orderId;
    private String paymentStatus;
    private String transactionId;
    private LocalDateTime paidAt;
}