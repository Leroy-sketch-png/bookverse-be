package com.example.bookverseserver.dto.response.Wishlist;

import com.example.bookverseserver.dto.response.Product.ListingResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectionResponse {
    String id; // Changed to String to match frontend
    String title;
    String description;
    List<ListingResponse> books; // Frontend expects books, not listings
    Integer totalBooks;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
