package com.example.bookverseserver.dto.response.Cart;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Cart financial summary (Vision API_CONTRACTS.md §Cart)
 */
@Builder
public record CartSummary(
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total
) {
}
