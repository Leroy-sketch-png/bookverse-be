package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.User.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "checkout_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckoutSession {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id")
  Cart cart;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  Order order;

  @Column(name = "payment_intent_id")
  String paymentIntentId;

  @Column(name = "client_secret")
  String clientSecret;

  @Column(nullable = false, length = 20)
  @Builder.Default
  String status = "PENDING";

  @Column(nullable = false, precision = 10, scale = 2)
  BigDecimal amount;

  @Column(length = 3)
  @Builder.Default
  String currency = "VND";

  @Column(name = "expires_at", nullable = false)
  LocalDateTime expiresAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  LocalDateTime createdAt;
}
