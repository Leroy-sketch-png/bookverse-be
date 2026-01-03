package com.example.bookverseserver.dto.response.Review;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewsListResponse {
    List<ReviewResponse> reviews;
    ReviewStatsResponse stats;
}
