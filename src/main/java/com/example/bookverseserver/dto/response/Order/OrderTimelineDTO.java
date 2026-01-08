package com.example.bookverseserver.dto.response.Order;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Order Timeline DTO - matches Vision API_CONTRACTS.md
 * FE expects timestamp as ISO string
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderTimelineDTO {
  String status;
  String timestamp; // ISO string format for FE compatibility
  String note;
}
