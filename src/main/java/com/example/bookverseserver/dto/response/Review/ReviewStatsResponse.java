package com.example.bookverseserver.dto.response.Review;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewStatsResponse {
    Double averageRating;
    Integer totalReviews;
    Map<Integer, Integer> ratingDistribution; // 1->count, 2->count, 3->count, 4->count, 5->count
}
