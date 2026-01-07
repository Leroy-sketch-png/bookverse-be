package com.example.bookverseserver.dto.response.CartItem;

import com.example.bookverseserver.dto.response.Cart.CartItemListingResponse;
import lombok.Builder;

/**
 * Cart item with full nested listing (Vision API_CONTRACTS.md §Cart)
 */
@Builder
public record CartItemForCartResponse(
        Long id,
        CartItemListingResponse listing,
        Integer quantity  // Quantity in cart
) {
}
