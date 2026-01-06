package com.example.bookverseserver.entity.Moderation;

import com.example.bookverseserver.entity.User.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Warning - Warnings issued to users for policy violations.
 * Per Vision features/moderation.md - warning system.
 */
@Entity
@Table(name = "warning")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Warning {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by", nullable = false)
    User issuedBy; // Moderator who issued the warning
    
    @Column(nullable = false, length = 100)
    String reason;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    String description;
    
    @Column(name = "severity", length = 20)
    @Builder.Default
    String severity = "MEDIUM"; // LOW, MEDIUM, HIGH
    
    @Column(name = "acknowledged")
    @Builder.Default
    Boolean acknowledged = false;
    
    @Column(name = "acknowledged_at")
    LocalDateTime acknowledgedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_action_id")
    ModerationAction relatedAction;
    
    @Column(name = "expires_at")
    LocalDateTime expiresAt; // When warning expires from record
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
}
