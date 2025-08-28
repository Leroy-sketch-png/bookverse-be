package com.example.bookverseserver.entity.User;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    User user;

    @Column(name = "display_name", length = 150)
    String displayName;

    @Column(name = "full_name")
    String fullName;

    @Column(name = "phone_number")
    String phoneNumber;

    @Column(name = "avatar_url")
    String avatarUrl;

    @Column
    String bio;

    @Column(name = "account_type")
    String accountType;

    @Column
    String location;

    @Column(name = "rating_avg")
    Double ratingAvg = 0.0;

    @Column(name = "rating_count")
    Integer ratingCount = 0;

    @Column(name = "seller_since")
    LocalDate sellerSince;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @UpdateTimestamp
    LocalDateTime updatedAt = LocalDateTime.now();
}