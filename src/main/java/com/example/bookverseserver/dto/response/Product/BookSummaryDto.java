package com.example.bookverseserver.dto.response.Product;

import com.example.bookverseserver.dto.response.Book.AuthorResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * Summary DTO for book information in listing responses.
 * Contains minimal book data needed for listing display.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookSummaryDto {
    Long id;
    String title;
    String author; // Primary author name (kept for backward compatibility)
    List<AuthorResponse> authors; // Full author details
    String isbn;
    String coverImage;
    BigDecimal averageRating;
    Integer totalReviews;
}
