package com.example.bookverseserver.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Public platform statistics - no sensitive data, accessible without auth.
 * Designed for homepage social proof and trust building.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicStatsResponse {
    
    Long totalBooks;
    Long totalSellers;
    Long totalOrders;
    Long totalUsers;
    Long categoryCount;
    Double avgRating;
    
    // Activity indicators (last 24h)
    Long ordersToday;
    Long newListingsToday;
}
