package com.example.bookverseserver.dto.response.Analytics;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueDataResponse {
    BigDecimal total;
    Double trend; // Percentage change
    List<DailyRevenue> byDay;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyRevenue {
        String date; // ISO date string
        BigDecimal amount;
    }
}
