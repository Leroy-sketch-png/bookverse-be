package com.example.bookverseserver.dto.request.Book;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookMetaCreationRequest {
    String title;
    String isbn;
    String description;
    String publisher;
    LocalDate publishedDate;
    Integer pages;
    BigDecimal listPrice;

    Set<Long> authorIds;
    Set<Long> categoryIds;
    List<BookImageRequest> images;
}
