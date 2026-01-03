package com.example.bookverseserver.dto.response.Order;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnavailableItemDTO {
  Long bookId;
  String title;
  Integer requestedQuantity;
  Integer availableStock;
}
