package com.example.bookverseserver.entity.Product;

import com.example.bookverseserver.entity.Order_Payment.OrderItem;
import com.example.bookverseserver.entity.User.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Review entity for transaction-based seller feedback.
 * 
 * MARKETPLACE PRINCIPLE: Reviews are on ORDER ITEMS (verified purchases),
 * not on books. This builds SELLER trust, not book ratings.
 * 
 * A buyer reviews: "Did the seller deliver as promised?"
 * - Was the book condition accurate?
 * - Was shipping fast?
 * - Was packaging good?
 */
@Entity
@Table(name = "review", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "order_item_id" }, name = "unique_order_item_review")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * The order item being reviewed (verified purchase).
     * Links to: Order -> User (buyer), Listing -> User (seller)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    OrderItem orderItem;

    /**
     * The listing that was purchased (denormalized for query efficiency).
     * Same as orderItem.listing, kept for easier queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    Listing listing;

    /**
     * The seller being reviewed (denormalized for query efficiency).
     * Same as orderItem.seller or listing.seller.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    User seller;

    /**
     * The buyer who wrote the review.
     * Same as orderItem.order.user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false)
    @Min(1)
    @Max(5)
    Integer rating;

    @Column(columnDefinition = "TEXT")
    @Size(max = 2000)
    String comment;

    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    Boolean isVisible = true;

    @Column(name = "is_hidden", nullable = false)
    @Builder.Default
    Boolean isHidden = false;

    @Column(name = "hidden_reason")
    String hiddenReason;

    @Column(name = "helpful_count", nullable = false)
    @Builder.Default
    Integer helpfulCount = 0;

    /**
     * Always true for this entity since reviews require delivered orders.
     */
    @Column(name = "verified_purchase", nullable = false)
    @Builder.Default
    Boolean verifiedPurchase = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
