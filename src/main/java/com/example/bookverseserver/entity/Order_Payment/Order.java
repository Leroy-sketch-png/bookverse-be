package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.User.ShippingAddress;
import com.example.bookverseserver.entity.User.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    User customer;

    @CreationTimestamp
    LocalDateTime orderDate;

    String orderStatus;   // PENDING, SHIPPED, DELIVERED, CANCELLED
    String paymentStatus; // PENDING, PAID, FAILED

    @Column(precision = 10, scale = 2)
    BigDecimal totalAmount;

    String paymentMethod;
    String deliveryMethod;
    String discountId;

    @ManyToOne
    @JoinColumn(name = "shipping_address_id")
    ShippingAddress shippingAddress;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    Payment payment;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<OrderItem> orderItems;
}
