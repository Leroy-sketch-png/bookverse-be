package com.example.bookverseserver.entity.Product;

import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.PromotionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Promotion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    User seller;
    
    @Column(nullable = false, length = 255)
    String name;
    
    @Column(name = "discount_percentage", nullable = false)
    Integer discountPercentage; // 0-100
    
    @Column(name = "start_date", nullable = false)
    LocalDateTime startDate;
    
    @Column(name = "end_date", nullable = false)
    LocalDateTime endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    PromotionStatus status = PromotionStatus.SCHEDULED;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "promotion_listing",
        joinColumns = @JoinColumn(name = "promotion_id"),
        inverseJoinColumns = @JoinColumn(name = "listing_id")
    )
    @Builder.Default
    Set<Listing> appliedListings = new HashSet<>();
    
    @Column(name = "total_revenue", precision = 12, scale = 2)
    BigDecimal totalRevenue;
    
    @Column(name = "items_sold")
    Integer itemsSold;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
}
