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
    String author;
    String isbn;
    String description;
    BigDecimal price;
    String coverImageUrl;
    LocalDate publishedDate;

    Long sellerId;
    String sellerName;

    Long categoryId;
    String categoryName;

    Integer stockQuantity;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
