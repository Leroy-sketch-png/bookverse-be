package com.example.bookverseserver.dto.response.Order;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerOrderItemResponse {
    String listingId;
    String title;
    String image;
    Integer quantity;
    BigDecimal price;
}
