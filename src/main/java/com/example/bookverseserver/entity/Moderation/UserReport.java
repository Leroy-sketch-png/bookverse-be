package com.example.bookverseserver.entity.Moderation;

import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.ReportPriority;
import com.example.bookverseserver.enums.ReportStatus;
import com.example.bookverseserver.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * UserReport - Reports submitted by users about sellers, listings, or orders.
 * Per Vision features/moderation.md §2 User Reports.
 */
@Entity
@Table(name = "user_report")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    User reporter;
    
    @Column(name = "reported_entity_type", nullable = false, length = 20)
    String reportedEntityType; // "seller", "listing", "review"
    
    @Column(name = "reported_entity_id", nullable = false)
    Long reportedEntityId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    User reportedUser; // The user being reported (if applicable)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_listing_id")
    Listing reportedListing; // The listing being reported (if applicable)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_order_id")
    Order relatedOrder; // Related order (if applicable)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 30)
    ReportType reportType;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    String description;
    
    @ElementCollection
    @CollectionTable(name = "report_evidence", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "evidence_url", length = 500)
    @Builder.Default
    List<String> evidenceUrls = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    ReportPriority priority = ReportPriority.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    ReportStatus status = ReportStatus.OPEN;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    User assignedTo; // Moderator assigned
    
    @Column(name = "resolution_note", columnDefinition = "TEXT")
    String resolutionNote;
    
    @Column(name = "resolved_at")
    LocalDateTime resolvedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
