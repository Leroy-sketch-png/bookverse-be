package com.example.bookverseserver.dto.response.Cart;

import com.example.bookverseserver.dto.response.Product.ListingResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    Long id;
    ListingResponse listing;
    Integer quantity;
    BigDecimal subtotal;
    LocalDateTime addedAt;
}
