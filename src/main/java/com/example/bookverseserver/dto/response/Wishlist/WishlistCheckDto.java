package com.example.bookverseserver.dto.response.Wishlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistCheckDto {
    private Boolean inWishlist;
    private Long wishlistItemId;
    private LocalDateTime addedAt;
}