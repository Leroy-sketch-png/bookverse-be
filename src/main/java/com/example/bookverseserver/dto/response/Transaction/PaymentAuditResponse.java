package com.example.bookverseserver.dto.response.Transaction;

import com.example.bookverseserver.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentAuditResponse {
    private Long id;                // ID của Payment
    private UUID orderId;           // ID của Order
    private BigDecimal amount;      // Số tiền
    private String currency;        // Đơn vị tiền (USD)
    private PaymentStatus status;   // PENDING, PAID...
    private String paymentMethod;   // STRIPE
    private String transactionId;   // Mã giao dịch Stripe (ch_...)
    private LocalDateTime paidAt;   // Thời gian thanh toán
    private LocalDateTime createdAt; // Thời gian tạo
}