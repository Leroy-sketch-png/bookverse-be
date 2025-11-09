package com.example.bookverseserver.dto.request.CartItem;

public record CartItemRequest(
        Long listingId,
        Integer quantity
) {
}
