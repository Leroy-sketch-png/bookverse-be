package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.User.ShippingAddress;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"order\"") // The table name "order" is a reserved SQL keyword, so it needs to be escaped.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Many orders can belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // user_id is NOT NULL in SQL
            User user;

    // An order is linked to a shipping address
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", nullable = false) // shipping_address_id is NOT NULL in SQL
            ShippingAddress shippingAddress;

    // An order can optionally have a voucher
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    Voucher voucher;

    // Status of the order, mapped from a Java enum to a database string
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false) // order_status is NOT NULL in SQL
            OrderStatus orderStatus = OrderStatus.PENDING;

    // Financial fields, using BigDecimal for precision
    @Column(nullable = false)
    BigDecimal subtotalAmount;

    @Column(nullable = false)
    BigDecimal discountAmount;

    @Column(nullable = false)
    BigDecimal shippingAmount;

    @Column(nullable = false)
    BigDecimal totalAmount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    // A one-to-many relationship with OrderItem entities
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderItem> items = new ArrayList<>();

    // A one-to-many relationship with Payment entities
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Payment> payments = new ArrayList<>();
}
