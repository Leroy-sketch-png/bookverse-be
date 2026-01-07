package com.example.bookverseserver.dto.response.Seller;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Review displayed on public seller profile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerProfileReviewResponse {
    Long id;
    String reviewerName;
    String reviewerInitials;
    Integer rating;
    String comment;
    LocalDateTime createdAt;
    Boolean verifiedPurchase;
}
