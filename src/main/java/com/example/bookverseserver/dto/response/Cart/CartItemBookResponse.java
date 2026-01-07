package com.example.bookverseserver.dto.response.Cart;

import lombok.Builder;

/**
 * Book info nested in cart item listing (Vision API_CONTRACTS.md §Cart)
 */
@Builder
public record CartItemBookResponse(
        String title,
        String coverImage
) {
}
