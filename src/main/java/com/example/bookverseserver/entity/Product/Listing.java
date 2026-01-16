package com.example.bookverseserver.entity.Product;

import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.BookCondition;
import com.example.bookverseserver.enums.ListingStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listing")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    BookMeta bookMeta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    Category category;

    @Column(name = "title_override")
    String titleOverride;

    @Column(nullable = false)
    BigDecimal price;

    @Column(name = "original_price")
    BigDecimal originalPrice;

    @Column(nullable = false)
    String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    BookCondition condition;

    @Column(nullable = false)
    @Builder.Default
    Integer quantity = 1;

    @Column(columnDefinition = "TEXT")
    String description;

    String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    ListingStatus status = ListingStatus.DRAFT;

    @Builder.Default
    Boolean visibility = true;

    @Column(name = "platform_fee_percent")
    BigDecimal platformFeePercent;

    @Column(name = "suggested_price_low")
    BigDecimal suggestedPriceLow;

    @Column(name = "suggested_price_high")
    BigDecimal suggestedPriceHigh;

    @Builder.Default
    Integer views = 0;
    @Builder.Default
    Integer likes = 0;

    @Column(name = "sold_count")
    @Builder.Default
    Integer soldCount = 0;

    // Shipping information
    @Column(name = "free_shipping")
    @Builder.Default
    Boolean freeShipping = false;

    @Column(name = "estimated_shipping_days")
    String estimatedShippingDays;

    @Column(name = "ships_from")
    String shipsFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_promotion_id")
    Promotion activePromotion;

    @Column(name = "last_viewed_at")
    LocalDateTime lastViewedAt;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    Long deletedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    // @BatchSize prevents N+1 queries when accessing photos in Specification-based queries
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.BatchSize(size = 50)
    @Builder.Default
    List<ListingPhoto> photos = new ArrayList<>();

    /**
     * Auto-update status based on stock quantity.
     * - When quantity becomes 0 and status is ACTIVE, change to SOLD_OUT
     * - When quantity becomes > 0 and status is SOLD_OUT, change back to ACTIVE
     */
    @PreUpdate
    protected void onUpdate() {
        if (quantity != null && quantity <= 0 && status == ListingStatus.ACTIVE) {
            status = ListingStatus.SOLD_OUT;
        } else if (quantity != null && quantity > 0 && status == ListingStatus.SOLD_OUT) {
            status = ListingStatus.ACTIVE;
        }
    }

    // Helper method to calculate final price with active promotion
    public BigDecimal getFinalPrice() {
        if (activePromotion != null
                && activePromotion.getStatus() == com.example.bookverseserver.enums.PromotionStatus.ACTIVE) {
            BigDecimal discount = price.multiply(BigDecimal.valueOf(activePromotion.getDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            return price.subtract(discount);
        }
        return price;
    }

    // Helper method to get discount info
    public java.util.Map<String, Object> getDiscountInfo() {
        if (activePromotion != null
                && activePromotion.getStatus() == com.example.bookverseserver.enums.PromotionStatus.ACTIVE) {
            return java.util.Map.of(
                    "type", "PERCENT",
                    "value", activePromotion.getDiscountPercentage());
        }
        return null;
    }

    /**
     * Calculate discount percentage between original and current price.
     * 
     * @return discount percentage (0-100) or null if no original price
     */
    public Integer getDiscountPercentage() {
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0
                && price != null && originalPrice.compareTo(price) > 0) {
            return originalPrice.subtract(price)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(originalPrice, 0, java.math.RoundingMode.HALF_UP)
                    .intValue();
        }
        return null;
    }
}
