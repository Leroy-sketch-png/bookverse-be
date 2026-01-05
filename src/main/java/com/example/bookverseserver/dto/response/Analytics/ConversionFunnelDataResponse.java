package com.example.bookverseserver.dto.response.Analytics;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversionFunnelDataResponse {
    List<ConversionStep> steps;
    Double overallConversionRate;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConversionStep {
        String stage; // 'views' | 'wishlist' | 'cart' | 'checkout' | 'purchase'
        Integer count;
        Double conversionRate;
    }
}
