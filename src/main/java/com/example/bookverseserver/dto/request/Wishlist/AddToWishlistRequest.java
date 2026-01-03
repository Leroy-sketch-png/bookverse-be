package com.example.bookverseserver.dto.request.Wishlist;

import jakarta.validation.constraints.NotNull;

public record AddToWishlistRequest(
        @NotNull(message = "Book ID is required")
        Long bookId
) {
}
