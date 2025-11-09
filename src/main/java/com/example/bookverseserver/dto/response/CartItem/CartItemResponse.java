package com.example.bookverseserver.dto.response.CartItem;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CartItemResponse(
        BigDecimal totalPrice,
        BigDecimal discount
) {
}
