package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.User.User;
import com.google.type.Decimal;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "total_price", precision = 12, scale = 2)
    @Builder.Default
    BigDecimal totalPrice = BigDecimal.ZERO;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<CartItem> cartItems = new HashSet<>();

    // Voucher mapping: maps to cart.voucher_id column in the DB.
    // Make it insertable/updatable so JPA can assign/remove a voucher on the cart.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    Voucher voucher;

    public BigDecimal getDiscount() {
        if (voucher == null || totalPrice == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountValue = voucher.getDiscountValue();
        if (voucher.getDiscountType().equals("PERCENTAGE")) {
            return totalPrice.multiply(discountValue).divide(new BigDecimal("100"));
        } else if (voucher.getDiscountType().equals("FIXED")) {
            return discountValue;
        } else {
            return BigDecimal.ZERO;
        }
    }
}
