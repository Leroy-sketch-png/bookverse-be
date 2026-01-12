package com.example.bookverseserver.dto.response.Analytics;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductPerformanceResponse {
    String listingId;  // ID of the Listing entity (not productId - there's no Product entity)
    String title;
    String imageUrl;
    BigDecimal price;
    Integer sales;
    BigDecimal revenue;
    Integer views;
    Double conversionRate; // Percentage
}
