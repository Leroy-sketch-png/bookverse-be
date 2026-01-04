package com.example.bookverseserver.dto.response.Product;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

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
    String author; // Primary author name
    String isbn;
    String coverImage;
    BigDecimal averageRating;
    Integer totalReviews;
}
