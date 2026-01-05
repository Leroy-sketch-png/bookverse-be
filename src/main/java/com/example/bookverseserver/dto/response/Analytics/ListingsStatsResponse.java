package com.example.bookverseserver.dto.response.Analytics;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListingsStatsResponse {
    Integer active;
    Integer outOfStock;
    Integer pending; // or draft
    Integer total;
}
