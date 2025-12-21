package com.example.bookverseserver.dto.response.CartItem;

import java.math.BigDecimal;

public record CartItemForCartResponse(
        Long id,
        Long listingId,
        Integer quantity,
        String title,
        BigDecimal subTotalPrice
) {
}
