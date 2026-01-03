package com.example.bookverseserver.dto.response.Analytics;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerStatsResponse {
    RevenueData revenue;
    SalesData sales;
    OrdersBreakdown orders;
    ListingsStats listings;
    ViewsData views;
    RatingData rating;
    BigDecimal avgOrderValue;
    Double conversionRate;
    WishlistData wishlistAdds;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WishlistData {
        Integer total;
        Double trend;
    }
}
