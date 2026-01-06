package com.example.bookverseserver.entity.Moderation;

import com.example.bookverseserver.entity.User.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Suspension - Account suspensions (temporary or permanent).
 * Per Vision features/moderation.md - suspension system.
 */
@Entity
@Table(name = "suspension")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Suspension {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suspended_by", nullable = false)
    User suspendedBy; // Moderator who issued the suspension
    
    @Column(nullable = false, length = 100)
    String reason;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    String description;
    
    @Column(name = "is_permanent")
    @Builder.Default
    Boolean isPermanent = false;
    
    @Column(name = "duration_days")
    Integer durationDays; // Null if permanent
    
    @Column(name = "starts_at", nullable = false)
    LocalDateTime startsAt;
    
    @Column(name = "ends_at")
    LocalDateTime endsAt; // Null if permanent
    
    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;
    
    @Column(name = "lifted_by")
    Long liftedBy; // Moderator who lifted the suspension (if applicable)
    
    @Column(name = "lifted_at")
    LocalDateTime liftedAt;
    
    @Column(name = "lift_reason", columnDefinition = "TEXT")
    String liftReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_action_id")
    ModerationAction relatedAction;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
