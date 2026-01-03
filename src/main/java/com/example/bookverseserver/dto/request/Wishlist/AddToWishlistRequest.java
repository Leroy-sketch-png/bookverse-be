package com.example.bookverseserver.dto.request.Wishlist;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddToWishlistRequest {
    @NotNull(message = "Listing ID is required")
    @Positive
    private Long listingId;
}
