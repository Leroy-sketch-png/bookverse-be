package com.example.bookverseserver.dto.response.Analytics;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrdersBreakdown {
    Integer pending;
    Integer processing;
    Integer shipped;
    Integer delivered;
    Integer cancelled;
}
