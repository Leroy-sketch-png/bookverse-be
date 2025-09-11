package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.SellerPayoutStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "seller_payout")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    User seller;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    SellerPayoutStatus status;

    @Column(nullable = false, length = 50)
    String method;

    @Column(name = "external_reference", length = 255)
    String externalReference;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    LocalDateTime createdAt;

    @Column(name = "paid_at")
    LocalDateTime paidAt;
}
