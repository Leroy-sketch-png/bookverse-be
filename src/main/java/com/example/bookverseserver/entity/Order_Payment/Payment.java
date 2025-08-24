package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.User.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    Order order;

    @ManyToOne
    @JoinColumn(name = "payer_id")
    User payer;

    String paymentMethod;
    BigDecimal amount;

    @Column(name = "payment_status")
    String paymentStatus = "PENDING"; // Enum có thể tách riêng

    String transactionId;
    LocalDateTime paidAt;
    LocalDateTime createdAt;
}
