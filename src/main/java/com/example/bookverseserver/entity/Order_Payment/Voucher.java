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
@Table(name = "voucher")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false, length = 50)
    String code;

    String description;

    @Column(nullable = false, length = 20)
    String discountType; // PERCENTAGE or FIXED

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal discountValue;

    BigDecimal minOrderAmount;
    Integer maxUsesPerUser;
    Integer totalUsesLimit;
    Boolean active;

    LocalDateTime startDate;
    LocalDateTime endDate;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<UserVoucher> userVouchers;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
