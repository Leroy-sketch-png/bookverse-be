package com.example.bookverseserver.dto.response.Wishlist;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WishlistResponse {
    List<WishlistItemResponse> items;
    Integer totalCount;
}
