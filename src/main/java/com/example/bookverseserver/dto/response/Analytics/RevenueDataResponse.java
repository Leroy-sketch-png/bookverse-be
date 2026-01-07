package com.example.bookverseserver.dto.response.Analytics;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * Revenue trend response matching FE Vision 2.0 types.
 * FE expects: { data: RevenueTrendDataPoint[], summary: {...} }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueDataResponse {
    List<RevenueTrendDataPoint> data;
    Summary summary;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevenueTrendDataPoint {
        String date;        // ISO date string "YYYY-MM-DD"
        BigDecimal revenue;
        Integer orders;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        BigDecimal totalRevenue;
        BigDecimal averageRevenue;
        PeakDay peakDay;
        Double trend;       // Percentage change vs previous period
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeakDay {
        String date;
        BigDecimal revenue;
    }
}
