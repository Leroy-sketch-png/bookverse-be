package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.User.UserVoucher;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Voucher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;


    String code;
    String description;
    String discountType;
    BigDecimal discountValue;
    BigDecimal minOrderValue;
    BigDecimal maxDiscountValue;
    String appliesTo;
    Long appliesToId;
    Boolean isActive = true;
    LocalDateTime validFrom;
    LocalDateTime validTo;


    @CreationTimestamp LocalDateTime createdAt;
    @UpdateTimestamp LocalDateTime updatedAt;
}
