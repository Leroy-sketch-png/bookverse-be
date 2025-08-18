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
public class BookUpdateRequest {
    String title;
    String author;

    String description;
    BigDecimal price;

    Long categoryId;
    String coverImageUrl;
    LocalDate publishedDate;

    // stock nằm trong Inventory, client có thể gửi ở đây
    Integer stockQuantity;
}
