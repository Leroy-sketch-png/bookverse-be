package com.example.bookverseserver.dto.request.Review;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for creating a transaction-based review.
 * 
 * MARKETPLACE MODEL: Reviews are on ORDER ITEMS.
 * The orderId and orderItemId are path parameters, not in request body.
 * Only rating and comment come in the body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReviewRequest {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    Integer rating;

    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    String comment;
}
