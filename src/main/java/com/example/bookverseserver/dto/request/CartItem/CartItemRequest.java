package com.example.bookverseserver.dto.request.CartItem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for adding/updating cart items.
 * Added validation annotations.
 */
public record CartItemRequest(
        @NotNull(message = "LISTING_ID_REQUIRED")
        Long listingId,
        
        @NotNull(message = "QUANTITY_REQUIRED")
        @Min(value = 1, message = "QUANTITY_MIN")
        Integer quantity
) {
}
