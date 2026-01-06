package com.example.bookverseserver.entity.Moderation;

import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.ModerationActionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ModerationAction - Actions taken by moderators.
 * Per Vision features/moderation.md - audit trail for all moderation decisions.
 */
@Entity
@Table(name = "moderation_action")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModerationAction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id", nullable = false)
    User moderator;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    ModerationActionType actionType;
    
    @Column(name = "target_type", nullable = false, length = 30)
    String targetType; // "flagged_listing", "user_report", "dispute", "user"
    
    @Column(name = "target_id", nullable = false)
    Long targetId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affected_user_id")
    User affectedUser; // User affected by this action
    
    @Column(nullable = false, columnDefinition = "TEXT")
    String reason;
    
    @Column(columnDefinition = "TEXT")
    String note; // Internal note
    
    @Column(name = "is_internal")
    @Builder.Default
    Boolean isInternal = false; // Internal note not shown to users
    
    @Column(name = "notify_parties")
    @Builder.Default
    Boolean notifyParties = true; // Send notifications to affected users
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
}
