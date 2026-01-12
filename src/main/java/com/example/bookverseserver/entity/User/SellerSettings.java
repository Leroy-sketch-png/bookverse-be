package com.example.bookverseserver.entity.User;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Seller-specific settings: shipping preferences, notification preferences, privacy settings.
 * Uses JSONB columns for flexibility in storing nested configuration objects.
 * 
 * NOTE: Uses HashMap (mutable) instead of Map.of() (immutable) to allow Hibernate
 * and service layer to modify the maps without UnsupportedOperationException.
 */
@Entity
@Table(name = "seller_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    User user;

    // Shipping settings stored as JSON - mutable HashMap for updates
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipping_settings", columnDefinition = "jsonb")
    @Builder.Default
    Map<String, Object> shippingSettings = new HashMap<>(Map.of(
        "standardShipping", true,
        "expressShipping", false,
        "localPickup", false,
        "freeShippingThreshold", 200000,
        "freeShippingEnabled", false
    ));

    // Notification settings stored as JSON - mutable HashMap for updates
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notification_settings", columnDefinition = "jsonb")
    @Builder.Default
    Map<String, Object> notificationSettings = new HashMap<>(Map.of(
        "emailNewOrder", true,
        "emailOrderShipped", true,
        "emailLowStock", true,
        "emailWeeklyReport", false,
        "pushNewOrder", true,
        "pushMessages", true
    ));

    // Privacy settings stored as JSON - mutable HashMap for updates
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "privacy_settings", columnDefinition = "jsonb")
    @Builder.Default
    Map<String, Object> privacySettings = new HashMap<>(Map.of(
        "showPhone", true,
        "allowMessages", true,
        "showStats", true
    ));

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
