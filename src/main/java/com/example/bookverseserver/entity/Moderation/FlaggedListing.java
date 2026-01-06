package com.example.bookverseserver.entity.Moderation;

import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.FlagSeverity;
import com.example.bookverseserver.enums.FlagStatus;
import com.example.bookverseserver.enums.FlagType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * FlaggedListing - Auto-flagged listings for moderator review.
 * Per Vision features/moderation.md §1 Content Moderation Queue.
 */
@Entity
@Table(name = "flagged_listing")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlaggedListing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    Listing listing;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "flag_type", nullable = false, length = 30)
    FlagType flagType;
    
    @Column(name = "flag_reason", length = 500)
    String flagReason;
    
    @Column(name = "confidence_score")
    Double confidenceScore; // 0.0 - 1.0
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    FlagSeverity severity = FlagSeverity.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    FlagStatus status = FlagStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    User reviewedBy;
    
    @Column(name = "review_note", columnDefinition = "TEXT")
    String reviewNote;
    
    @Column(name = "reviewed_at")
    LocalDateTime reviewedAt;
    
    @Column(name = "auto_action", length = 50)
    String autoAction; // Action taken automatically (if any)
    
    @CreationTimestamp
    @Column(name = "flagged_at", updatable = false)
    LocalDateTime flaggedAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
