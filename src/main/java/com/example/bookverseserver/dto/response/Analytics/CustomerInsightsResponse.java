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
public class CustomerInsightsResponse {
    Integer totalCustomers;
    Integer repeatCustomers;      // Renamed from returningCustomers to match FE
    Double repeatRate;            // Renamed from repeatPurchaseRate to match FE
    Integer newCustomers;
    Double newCustomersTrend;     // Added to match FE
    BigDecimal avgLifetimeValue;  // Renamed from averageOrderValue to match FE
    List<TopRegion> topRegions;   // Added to match FE
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopRegion {
        String region;
        Integer orders;
        Double percentage;
    }
}
