package com.example.bookverseserver.dto.response.Analytics;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SalesDataResponse {
    Integer total;
    Double trend; // Percentage change
    List<DailySales> byDay;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailySales {
        String date; // ISO date string
        Integer count;
    }
}
