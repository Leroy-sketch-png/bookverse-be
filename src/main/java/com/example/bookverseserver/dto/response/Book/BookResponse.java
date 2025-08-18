package com.example.bookverseserver.dto.response.Book;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookResponse {
    Long id;
    String title;
    String authorName;     // resolved from Author entity
    Long authorId;

    String categoryName;   // resolved from Category entity
    Long categoryId;

    String sellerName;
    Long sellerId;

    String isbn;
    String description;
    BigDecimal price;
    BigDecimal listPrice;
    String condition;
    String coverImageUrl;
    LocalDate publishedDate;
    Integer stockQuantity;

    // Metadata
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
