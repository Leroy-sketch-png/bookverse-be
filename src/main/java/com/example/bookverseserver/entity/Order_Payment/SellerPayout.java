package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.User.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerPayout {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;


    @ManyToOne @JoinColumn(name = "seller_id")
    User seller;


    BigDecimal amount;
    String status;
    String method;
    String externalReference;


    @CreationTimestamp
    LocalDateTime createdAt;
    LocalDateTime paidAt;
}