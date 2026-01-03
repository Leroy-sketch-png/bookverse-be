package com.example.bookverseserver.dto.response.Analytics;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SalesData {
    Integer total;
    Double trend; // percentage change
    List<DailyData> byDay;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyData {
        String date; // yyyy-MM-dd
        Integer count;
    }
}
