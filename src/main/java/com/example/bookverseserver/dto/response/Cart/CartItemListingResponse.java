package com.example.bookverseserver.dto.response.Cart;

import com.example.bookverseserver.enums.BookCondition;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Listing info nested in cart item (Vision API_CONTRACTS.md §Cart)
 * Contains book, price, stock, condition, and seller info.
 */
@Builder
public record CartItemListingResponse(
        Long id,
        CartItemBookResponse book,
        BigDecimal price,
        BigDecimal finalPrice,
        Integer quantity,  // Available stock
        BookCondition condition,
        CartItemSellerResponse seller
) {
}
