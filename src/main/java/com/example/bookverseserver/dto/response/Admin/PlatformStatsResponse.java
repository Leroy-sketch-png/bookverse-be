package com.example.bookverseserver.dto.response.Admin;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Platform statistics response for admin dashboard.
 * Per Vision API_CONTRACTS.md - GET /admin/stats
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlatformStatsResponse {
    
    UserStats users;
    RevenueStats revenue;
    ListingStats listings;
    IssueStats issues;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserStats {
        Long total;
        Double trend;
        Long buyers;
        Long sellers;
        Long proSellers;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevenueStats {
        BigDecimal total;
        Double trend;
        BigDecimal platformFee;
        Long transactionCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListingStats {
        Long active;
        Double trend;
        Long available;
        Long sold;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IssueStats {
        Long pending;
        Double trend;
        Long moderation;
        Long disputes;
        Long verifications;
    }
}
