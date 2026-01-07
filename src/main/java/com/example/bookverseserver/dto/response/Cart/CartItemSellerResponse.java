package com.example.bookverseserver.dto.response.Cart;

import lombok.Builder;

/**
 * Seller info nested in cart item listing (Vision API_CONTRACTS.md §Cart)
 */
@Builder
public record CartItemSellerResponse(
        Long id,
        String name
) {
}
