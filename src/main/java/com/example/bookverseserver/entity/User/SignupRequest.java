package com.example.bookverseserver.entity.User;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "signup_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SignupRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = true, unique = true)
     String username;

    @Column(nullable = false)
     String passwordHash;

    @Column(nullable = false, length = 128)
     String otpHash;

    @Column(nullable = false)
     Instant expiresAt;

    @Column(nullable = false)
     Instant createdAt;

    @Column(nullable = true)
     Integer attempts = 0;
}

