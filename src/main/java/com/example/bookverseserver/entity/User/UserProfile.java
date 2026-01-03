package com.example.bookverseserver.entity.User;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
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

    /**
     * Keeps the existing relation: one-to-one with User (unique constraint at DB level).
     * Repository method findByUser_Id(...) will work with this mapping.
     */
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
    
    @Column(name = "cover_image_url", columnDefinition = "TEXT")
    String coverImageUrl;

    @Column
    String bio;

    @Column(name = "account_type")
    String accountType;

    @Column
    String location;
    
    @Column(name = "is_pro_seller")
    @Builder.Default
    Boolean isProSeller = false;
    
    @Column(name = "response_time", length = 50)
    String responseTime;
    
    @Column(name = "fulfillment_rate", precision = 5, scale = 2)
    BigDecimal fulfillmentRate;

    /**
     * Ensure Lombok builder preserves default values:
     * use @Builder.Default so when builder is used and field omitted, default remains.
     */
    @Builder.Default
    @Column(name = "rating_avg")
    Double ratingAvg = 0.0;

    @Builder.Default
    @Column(name = "rating_count")
    Integer ratingCount = 0;

    @Column(name = "seller_since")
    LocalDate sellerSince;

    @Column(name = "preferences", columnDefinition = "text")
    /**
     * Flexible JSON blob stored as text. This is the portable option:
     * DB-agnostic, simple to use. If you later want structured preferences,
     * see the converter below to map to Map<String,Object>.
     */
            String preferences;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @UpdateTimestamp
    LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Optimistic locking to help with concurrent updates to the same profile.
     * Add handling in the service layer to retry/handle OptimisticLockException if desired.
     */
    @Version
    Integer version;
}
