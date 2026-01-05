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
    PaginationMeta meta;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationMeta {
        Integer page;
        Integer totalPages;
        Long totalItems;
        Integer itemsPerPage;
    }
}
