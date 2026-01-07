package com.example.bookverseserver.dto.response.Review;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Response DTO for transaction-based reviews.
 * 
 * Reviews are on ORDER ITEMS (verified purchases).
 * They build SELLER trust in the marketplace.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {
    Long id;
    Integer rating;
    String comment;
    Boolean verifiedPurchase;
    Integer helpfulCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    
    // Reviewer info (anonymized for privacy)
    String reviewerName;
    String reviewerInitials;
    String reviewerAvatarUrl;
    
    // Links to marketplace entities
    Long listingId;
    Long sellerId;
    
    // Current user context (set by service)
    Boolean isCurrentUserReview;
    Boolean userHasVotedHelpful;
}
