package com.example.bookverseserver.entity.Moderation;

import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dispute - Order disputes between buyer and seller.
 * Per Vision features/moderation.md §3 Disputes.
 */
@Entity
@Table(name = "dispute")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Dispute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    User buyer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    User seller;
    
    @Column(nullable = false, length = 100)
    String reason;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    String description;
    
    @Column(name = "disputed_amount", precision = 12, scale = 2)
    BigDecimal disputedAmount;
    
    @ElementCollection
    @CollectionTable(name = "dispute_evidence", joinColumns = @JoinColumn(name = "dispute_id"))
    @Column(name = "evidence_url", length = 500)
    @Builder.Default
    List<String> evidenceUrls = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    DisputeStatus status = DisputeStatus.OPEN;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    User assignedTo; // Moderator handling the dispute
    
    @Column(name = "seller_response", columnDefinition = "TEXT")
    String sellerResponse;
    
    @Column(name = "seller_responded_at")
    LocalDateTime sellerRespondedAt;
    
    @Column(name = "resolution", columnDefinition = "TEXT")
    String resolution;
    
    @Column(name = "refund_amount", precision = 12, scale = 2)
    BigDecimal refundAmount;
    
    @Column(name = "resolved_at")
    LocalDateTime resolvedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
