package com.example.bookverseserver.dto.response.Seller;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

/**
 * Reviews response for public seller profile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerProfileReviewsWrapper {
    List<SellerProfileReviewResponse> reviews;
    ReviewStats stats;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewStats {
        Double averageRating;
        Integer totalReviews;
        Map<String, Integer> ratingDistribution; // "5" -> count, "4" -> count, etc.
    }
}
