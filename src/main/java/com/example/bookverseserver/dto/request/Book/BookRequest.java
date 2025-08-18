package com.example.bookverseserver.dto.request.Book;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookRequest {
    String title;
    Long authorId;        // reference to Author entity
    Long categoryId;      // reference to Category entity
    String isbn;
    String description;
    BigDecimal price;     // selling price
    BigDecimal listPrice; // strikethrough price (optional)
    String condition;     // "New", "Like New", "Used", "Acceptable"
    String coverImageUrl;
    LocalDate publishedDate;
    Integer stockQuantity; // client may set initial stock
}
