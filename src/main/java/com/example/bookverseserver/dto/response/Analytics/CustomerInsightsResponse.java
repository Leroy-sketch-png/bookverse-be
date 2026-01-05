package com.example.bookverseserver.dto.response.Analytics;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerInsightsResponse {
    Integer totalCustomers;
    Integer newCustomers;
    Integer returningCustomers;
    Double repeatPurchaseRate;
    BigDecimal averageOrderValue;
    Integer totalOrders;
}
