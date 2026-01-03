package com.example.bookverseserver.dto.request.Review;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReviewRequest {
    
    @NotNull(message = "Book ID is required")
    Long bookId;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    Integer rating;
    
    @NotBlank(message = "Comment is required")
    @Size(min = 10, max = 1000, message = "Comment must be between 10 and 1000 characters")
    String comment;
}
