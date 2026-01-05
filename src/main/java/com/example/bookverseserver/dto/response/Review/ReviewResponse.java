package com.example.bookverseserver.dto.response.Review;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {
    String id; // Changed to String to match frontend expectations
    String bookId; // Changed to String to match frontend expectations
    Long userId;
    String userName;
    String userAvatar;
    Integer rating;
    String comment;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Integer helpful; // Match frontend naming (not helpfulCount)
    Boolean verified; // verified purchase
    Boolean isCurrentUserReview;
    Boolean userHasVotedHelpful;
}
