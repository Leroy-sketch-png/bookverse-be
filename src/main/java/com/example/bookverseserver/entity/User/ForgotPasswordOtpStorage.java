package com.example.bookverseserver.entity.User;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "forgot_password")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForgotPasswordOtpStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false, length = 128)
    String otpHash;

    @Column(nullable = false)
    Instant expiresAt;

    @Column(nullable = false)
    Instant createdAt;

    @Column(nullable = true)
    Integer attempts = 0;
}
