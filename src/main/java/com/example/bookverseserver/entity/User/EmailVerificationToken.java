package com.example.bookverseserver.entity.User;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailVerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 255)
    String email;

    @Column(nullable = false, length = 10)
    String otp;

    @Column(name = "expiry_date", nullable = false)
    LocalDateTime expiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
}
