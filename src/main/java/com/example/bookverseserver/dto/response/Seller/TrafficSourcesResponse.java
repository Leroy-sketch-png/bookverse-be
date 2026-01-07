package com.example.bookverseserver.dto.response.Seller;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Traffic sources analytics response.
 * STUB: Returns placeholder data until real analytics are implemented.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrafficSourcesResponse {
    List<TrafficSource> sources;
    Long totalTraffic;
    Double bounceRate;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrafficSource {
        String source;
        Long visitors;
        Double percentage;
    }
}
