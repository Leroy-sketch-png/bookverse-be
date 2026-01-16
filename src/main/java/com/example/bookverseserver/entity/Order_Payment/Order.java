package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.User.ShippingAddress;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // Changed from "order" to "orders" to match schema and avoid keywords
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        User user;

        @Column(name = "order_number", unique = true, nullable = false, length = 20)
        String orderNumber;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        @Builder.Default
        OrderStatus status = OrderStatus.PENDING;

        @Column(nullable = false, precision = 10, scale = 2)
        BigDecimal subtotal;

        @Column(nullable = false, precision = 10, scale = 2)
        BigDecimal totalAmount;

        @Column(nullable = false, precision = 10, scale = 2)
        @Builder.Default
        BigDecimal tax = BigDecimal.ZERO;

        @Column(nullable = false, precision = 10, scale = 2)
        @Builder.Default
        BigDecimal shipping = BigDecimal.ZERO;

        @Column(nullable = false, precision = 10, scale = 2)
        @Builder.Default
        BigDecimal discount = BigDecimal.ZERO;

        @Column(nullable = false, precision = 10, scale = 2)
        BigDecimal total;

        @Column(name = "promo_code", length = 50)
        String promoCode;

        @Column(columnDefinition = "TEXT")
        String notes;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "shipping_address_id")
        ShippingAddress shippingAddress;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "billing_address_id")
        ShippingAddress billingAddress;

        @Column(name = "tracking_number", length = 100)
        String trackingNumber;

        @Column(name = "tracking_url", columnDefinition = "TEXT")
        String trackingUrl;

        @Column(length = 50)
        String carrier;

        @Column(name = "estimated_delivery")
        LocalDateTime estimatedDelivery;

        @Column(name = "shipped_at")
        LocalDateTime shippedAt;

        @Column(name = "delivered_at")
        LocalDateTime deliveredAt;

        @Column(name = "cancelled_at")
        LocalDateTime cancelledAt;

        @Column(name = "cancellation_reason", columnDefinition = "TEXT")
        String cancellationReason;

        @CreationTimestamp
        @Column(name = "created_at", updatable = false)
        LocalDateTime createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at")
        LocalDateTime updatedAt;

        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
        @BatchSize(size = 50)
        @Builder.Default
        List<OrderItem> items = new ArrayList<>();

        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
        @BatchSize(size = 50)
        @Builder.Default
        List<OrderTimeline> timeline = new ArrayList<>();

        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
        @BatchSize(size = 50)
        List<Payment> payments = new ArrayList<>();
}
