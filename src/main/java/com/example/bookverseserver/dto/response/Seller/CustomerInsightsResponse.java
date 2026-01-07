package com.example.bookverseserver.dto.response.Seller;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * Customer insights analytics response.
 * STUB: Returns placeholder data until real analytics are implemented.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerInsightsResponse {
    Long totalCustomers;
    Long repeatCustomers;
    Double repeatRate;
    Long newCustomers;
    Double newCustomersTrend;
    BigDecimal avgLifetimeValue;
    List<RegionStats> topRegions;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegionStats {
        String region;
        Long orders;
        Double percentage;
    }
}
