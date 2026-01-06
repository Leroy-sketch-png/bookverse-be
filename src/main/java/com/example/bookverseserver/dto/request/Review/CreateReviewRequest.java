package com.example.bookverseserver.dto.request.Review;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for creating a review.
 * Per Vision (buyer-flow.md): Reviews require orderId to verify purchase.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReviewRequest {

    @NotNull(message = "Order ID is required to verify purchase")
    Long orderId;

    @NotNull(message = "Listing ID is required")
    Long listingId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    Integer rating;

    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    String comment;
}
