package com.example.bookverseserver.dto.response.Wishlist;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class WishlistResponse {
    private List<WishlistItemDTO> favorites;
    private long totalFavorites;
}
