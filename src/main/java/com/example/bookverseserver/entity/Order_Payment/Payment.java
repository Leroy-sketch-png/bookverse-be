package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // QUAN TRỌNG: Map ngược lại với biến "items" trong Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    // Map với User (giả sử User cũng dùng Long ID)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "payment_intent_id", unique = true)
    String paymentIntentId;

    @Column(nullable = false)
    BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_method")
    String paymentMethod;

    @Column(name = "transaction_id")
    String transactionId;

    @Column(name = "receipt_url", columnDefinition = "TEXT")
    String receiptUrl;

    @Column(name = "paid_at")
    LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
}