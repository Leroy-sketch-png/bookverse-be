package com.example.bookverseserver.dto.response.Analytics;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrafficSourcesDataResponse {
    List<TrafficSource> sources;
    Integer totalTraffic;
    Double bounceRate;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrafficSource {
        String source; // 'direct' | 'organic' | 'social' | 'referral' | 'email'
        Double percentage;
        Integer visits;
    }
}
