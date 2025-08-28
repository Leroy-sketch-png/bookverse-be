package com.example.bookverseserver.entity.Order_Payment;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "voucher")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true, length = 50)
    String code;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(name = "discount_type", nullable = false, length = 20)
    String discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    BigDecimal discountValue;

    @Column(name = "min_order_value", precision = 10, scale = 2)
    BigDecimal minOrderValue;

    @Column(name = "applies_to", length = 20)
    String appliesTo;

    @Column(name = "applies_to_id")
    Long appliesToId;

    @Column(name = "is_active", nullable = false)
    Boolean isActive = true;

    @Column(name = "valid_from")
    LocalDateTime validFrom;

    @Column(name = "valid_to")
    LocalDateTime validTo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;
}
