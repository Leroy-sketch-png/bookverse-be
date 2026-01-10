package com.example.bookverseserver.dto.response.Wishlist;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WishlistResponse {
    Long id;
    Integer totalBooks;
    Integer totalCollections;
    Long totalFavorites; // For pagination
    List<WishlistItemDTO> items; // Actual wishlist items
    List<CollectionResponse> collections;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
