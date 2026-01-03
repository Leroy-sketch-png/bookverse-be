package com.example.bookverseserver.dto.response.Wishlist;

import com.example.bookverseserver.dto.response.Book.BookResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WishlistItemResponse {
    Long id;
    BookResponse book;
    LocalDateTime addedAt;
}
