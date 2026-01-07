package com.example.bookverseserver.dto.response.Seller;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Conversion funnel analytics response.
 * STUB: Returns placeholder data until real analytics are implemented.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversionFunnelResponse {
    List<FunnelStep> steps;
    Double overallConversionRate;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FunnelStep {
        String stage; // views, wishlist, cart, checkout, purchase
        Long count;
        Double conversionRate;
    }
}
