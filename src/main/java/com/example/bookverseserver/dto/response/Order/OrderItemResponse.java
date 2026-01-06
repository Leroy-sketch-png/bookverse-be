package com.example.bookverseserver.dto.response.Order;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    Long id;
    Long listingId;
    String title;
    String author;
    String coverImage;
    Integer quantity;
    BigDecimal price;
    BigDecimal subtotal;
}
